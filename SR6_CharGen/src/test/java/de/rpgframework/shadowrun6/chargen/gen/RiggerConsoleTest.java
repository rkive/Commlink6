package de.rpgframework.shadowrun6.chargen.gen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rpgframework.genericrpg.Possible;
import de.rpgframework.genericrpg.data.AttributeValue;
import de.rpgframework.genericrpg.items.CarriedItem;
import de.rpgframework.shadowrun.MagicOrResonanceType;
import de.rpgframework.shadowrun.Priority;
import de.rpgframework.shadowrun.PriorityType;
import de.rpgframework.shadowrun.ShadowrunAttribute;
import de.rpgframework.shadowrun.chargen.gen.PriorityAttributeGenerator;
import de.rpgframework.shadowrun6.SR6MetaType;
import de.rpgframework.shadowrun6.SR6Quality;
import de.rpgframework.shadowrun6.Shadowrun6Character;
import de.rpgframework.shadowrun6.Shadowrun6Core;
import de.rpgframework.shadowrun6.Shadowrun6Tools;
import de.rpgframework.shadowrun6.chargen.gen.priority.PriorityCharacterGenerator;
import de.rpgframework.shadowrun6.data.Shadowrun6DataPlugin;
import de.rpgframework.shadowrun6.items.ItemTemplate;
import de.rpgframework.shadowrun6.items.SR6ItemAttribute;

public class RiggerConsoleTest {

	private Shadowrun6Character model;
	private PriorityCharacterGenerator charGen;

	@BeforeClass
	public static void setupClass() {
		Locale.setDefault(Locale.ENGLISH);
		Shadowrun6DataPlugin plugin = new Shadowrun6DataPlugin();
		plugin.init();
	}

	@Before
	public void setup() {
		model = new Shadowrun6Character();
		charGen = new PriorityCharacterGenerator();
		charGen.setModel(model, null);
		charGen.getPriorityController().setPriority(PriorityType.ATTRIBUTE, Priority.A);
		charGen.getPriorityController().setPriority(PriorityType.MAGIC, Priority.B);
		charGen.getPriorityController().setPriority(PriorityType.METATYPE, Priority.C);
		charGen.getPriorityController().setPriority(PriorityType.RESOURCES, Priority.D);
		charGen.getPriorityController().setPriority(PriorityType.SKILLS, Priority.E);
		charGen.getMetatypeController().select(Shadowrun6Core.getItem(SR6MetaType.class, "dwarf"));
		charGen.getMagicOrResonanceController().select(Shadowrun6Core.getItem(MagicOrResonanceType.class, "technomancer"));
		raiseAttributeTo(ShadowrunAttribute.RESONANCE, 6);
		raiseAttributeTo(ShadowrunAttribute.LOGIC, 5);
		raiseAttributeTo(ShadowrunAttribute.WILLPOWER, 4);
		raiseAttributeTo(ShadowrunAttribute.CHARISMA, 3);
		assertTrue(charGen.getQualityController().select(Shadowrun6Core.getItem(SR6Quality.class, "machinists")).wasSuccessful());
	}

	private void raiseAttributeTo(ShadowrunAttribute key, int target) {
		PriorityAttributeGenerator attribs = (PriorityAttributeGenerator) charGen.getAttributeController();
		AttributeValue<ShadowrunAttribute> val = model.getAttribute(key);
		while (val.getModifiedValue()<target) {
			Possible poss = attribs.canBeIncreased(val);
			assertTrue("May not increase "+key+" to "+(val.getModifiedValue()+1)+": "+poss, poss.get());
			assertTrue("Failed raising "+key+" from "+val.getModifiedValue(), attribs.increase(val).wasSuccessful());
		}
		assertEquals(target, val.getModifiedValue());
	}

	private int value(CarriedItem<ItemTemplate> item, SR6ItemAttribute attrib) {
		assertNotNull(attrib+" missing on "+item.getKey(), item.getAsValue(attrib));
		return item.getAsValue(attrib).getModifiedValue();
	}

	@Test
	public void testMachinistConsoleHasTheLivingPersonaValues() {
		charGen.runProcessors();

		CarriedItem<ItemTemplate> console = Shadowrun6Tools.getBestRCC(model);

		assertNotNull(console);
		assertEquals("machinist_console", console.getKey());
		assertEquals(6, value(console, SR6ItemAttribute.DEVICE_RATING));
		assertEquals(5, value(console, SR6ItemAttribute.DATA_PROCESSING));
		assertEquals(4, value(console, SR6ItemAttribute.FIREWALL));
		assertEquals(3, value(console, SR6ItemAttribute.CONCURRENT_PROGRAMS));
		assertEquals(4, value(console, SR6ItemAttribute.NOISE_REDUCTION));
	}

	@Test
	public void testMachinistConsoleOutranksAWeakerBoughtConsole() {
		assertTrue(charGen.getEquipmentController().select(Shadowrun6Core.getItem(ItemTemplate.class, "scratch_built_junk")).wasSuccessful());
		charGen.runProcessors();

		CarriedItem<ItemTemplate> console = Shadowrun6Tools.getBestRCC(model);

		assertNotNull(console);
		assertEquals("machinist_console", console.getKey());
		assertEquals(6, value(console, SR6ItemAttribute.DEVICE_RATING));
	}

}
