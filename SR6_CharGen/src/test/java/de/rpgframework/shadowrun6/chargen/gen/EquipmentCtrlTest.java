package de.rpgframework.shadowrun6.chargen.gen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rpgframework.genericrpg.Possible;
import de.rpgframework.genericrpg.chargen.OperationResult;
import de.rpgframework.genericrpg.data.Decision;
import de.rpgframework.genericrpg.items.CarriedItem;
import de.rpgframework.genericrpg.items.CarryMode;
import de.rpgframework.genericrpg.items.GearTool;
import de.rpgframework.genericrpg.modification.DataItemModification;
import de.rpgframework.genericrpg.modification.Modification;
import de.rpgframework.genericrpg.modification.ValueModification;
import de.rpgframework.shadowrun.chargen.charctrl.IRejectReasons;
import de.rpgframework.shadowrun.items.AugmentationQuality;
import de.rpgframework.shadowrun6.CreatePoints;
import de.rpgframework.shadowrun6.Shadowrun6Character;
import de.rpgframework.shadowrun6.Shadowrun6Core;
import de.rpgframework.shadowrun6.Shadowrun6Tools;
import de.rpgframework.shadowrun6.chargen.charctrl.ISR6EquipmentController;
import de.rpgframework.shadowrun6.chargen.charctrl.SR6CharacterGenerator;
import de.rpgframework.shadowrun6.chargen.gen.priority.SR6PrioritySettings;
import de.rpgframework.shadowrun6.data.Shadowrun6DataPlugin;
import de.rpgframework.shadowrun6.items.ItemHook;
import de.rpgframework.shadowrun6.items.ItemTemplate;
import de.rpgframework.shadowrun6.items.ItemUtil;
import de.rpgframework.shadowrun6.items.SR6GearTool;
import de.rpgframework.shadowrun6.items.SR6ItemAttribute;
import de.rpgframework.shadowrun6.modifications.ShadowrunReference;
import de.rpgframework.shadowrun6.proc.ResetModifications;

/**
 * @author prelle
 *
 */
public class EquipmentCtrlTest {

	private Shadowrun6Character model;
	private ISR6EquipmentController ctrl;
	private SR6CharacterGenerator charGen;
	private List<Modification> preMods = new ArrayList<>();

	//-------------------------------------------------------------------
	@BeforeClass
	public static void setupClass() {
		Shadowrun6DataPlugin plugin = new Shadowrun6DataPlugin();
		plugin.init();

	}

	//-------------------------------------------------------------------
	@Before
	public void setup() {
		model = new Shadowrun6Character();
		model.setCharGenSettings(new SR6PrioritySettings());
		preMods.clear();
		charGen = new SR6TestGenerator(model) {
			public ISR6EquipmentController ISR6EquipmentController() {
				return ctrl;
			}
			public void runProcessors() {
				System.out.println("---------------");
				ctrl.process(preMods);
			}
		};
		ctrl  = new SR6EquipmentGenerator(charGen);
		charGen.runProcessors();
	}

	//-------------------------------------------------------------------
	@Test
	public void testSimple() {
		ItemTemplate jacket = Shadowrun6Core.getItem(ItemTemplate.class, "synthleather_jacket");
		assertNotNull(jacket);

		Possible poss = ctrl.canBeSelected(jacket);
		// Should not be possible due to not enough nuyen
		assertFalse(poss.toString(), poss.get());
		assertEquals(IRejectReasons.IMPOSS_NOT_ENOUGH_NUYEN, poss.getI18NKey().get(0).getKey());

		// Set enough nuyen
		preMods.add(new ValueModification(ShadowrunReference.CREATION_POINTS, CreatePoints.NUYEN.name(), 300));
		charGen.runProcessors();
		poss = ctrl.canBeSelected(jacket);
		assertTrue(poss.toString(), poss.get());
		OperationResult<CarriedItem<ItemTemplate>> res = ctrl.select(jacket);
		assertTrue(res.wasSuccessful());
		assertNotNull(res.get());
		// Nuyen should be 0
		assertEquals("Wrong nuyen paid", 0, model.getNuyen());
		assertEquals(1, res.get().getCount());
	}

	//-------------------------------------------------------------------
	@Test
	public void testCountable() {
		ItemTemplate jacket = Shadowrun6Core.getItem(ItemTemplate.class, "metal_restraints");
		assertNotNull(jacket);

		// Set enough nuyen
		preMods.add(new ValueModification(ShadowrunReference.CREATION_POINTS, CreatePoints.NUYEN.name(), 60));
		charGen.runProcessors();
		OperationResult<CarriedItem<ItemTemplate>> res = ctrl.select(jacket);
		assertTrue(res.wasSuccessful());
		assertNotNull(res.get());
		// Nuyen should be 40
		assertEquals("Wrong nuyen paid", 40, model.getNuyen());
		assertEquals("Countable not detected",1, res.get().getCount());

		CarriedItem<ItemTemplate> countable = res.get();
		Possible poss = ctrl.canBeIncreased(countable);
		assertTrue(poss.get());
		poss = ctrl.canBeDecreased(countable);
		assertFalse(poss.get());
//		assertEquals(IRejectReasons.IMPOSS_MIN_LEVEL_REACHED, poss.getI18NKey().get(0).getKey());
		ctrl.increase(countable);
		assertEquals(2, res.get().getCount());
		assertEquals("Wrong nuyen paid", 20, model.getNuyen());
		poss = ctrl.canBeIncreased(countable);
		assertTrue("Expected to be increasable: "+poss.getMostSevere(),poss.get());
		ctrl.increase(countable);
		assertEquals("Increasing did not work",3, res.get().getCount());
		assertEquals("Wrong nuyen paid", 0, model.getNuyen());

		poss = ctrl.canBeIncreased(countable);
		// Should not be possible due to not enough nuyen
		assertFalse("Increasing above Nuyen limit not detected: "+poss.getMostSevere(), poss.get());
		assertEquals(IRejectReasons.IMPOSS_NOT_ENOUGH_NUYEN, poss.getI18NKey().get(0).getKey());
		poss = ctrl.canBeDecreased(countable);
		assertTrue(poss.get());
		ctrl.decrease(countable);
		assertEquals(2, res.get().getCount());
		assertEquals("Wrong nuyen paid", 20, model.getNuyen());
	}

	//-------------------------------------------------------------------
	@Test
	public void testEmbedding() {
		// Set enough nuyen
		preMods.add(new ValueModification(ShadowrunReference.CREATION_POINTS, CreatePoints.NUYEN.name(), 600));
		charGen.runProcessors();
		OperationResult<CarriedItem<ItemTemplate>> res = ctrl.select(Shadowrun6Core.getItem(ItemTemplate.class, "beretta_101T"));
		assertTrue(res.wasSuccessful());
		CarriedItem<ItemTemplate> container = res.get();
		assertEquals("Wrong nuyen paid", 340, model.getNuyen());

		assertNotNull(container.getSlot(ItemHook.TOP));
		assertNull(container.getSlot(ItemHook.UNDER));
		assertTrue("Unexpected embedded items in TOP slot: "+container.getSlot(ItemHook.TOP).getAllEmbeddedItems(),container.getSlot(ItemHook.TOP).getAllEmbeddedItems().isEmpty());

		ItemTemplate peri = Shadowrun6Core.getItem(ItemTemplate.class, "periscope");
		ItemTemplate bipod = Shadowrun6Core.getItem(ItemTemplate.class, "bipod");
		assertNotNull(peri);

		Possible poss = ctrl.canBeSelected(peri);
		// Should not be possible due to no slot
		assertFalse(poss.toString(), poss.get());
		assertEquals(IRejectReasons.IMPOSS_INVALID_CARRYMODE, poss.getI18NKey().get(0).getKey());

		// Try embedding
		poss = ctrl.canBeEmbedded(container, ItemHook.TOP, peri, null);
		assertTrue(poss.toString(), poss.get());
		// Embedding in a non existing slot
		poss = ctrl.canBeEmbedded(container, ItemHook.UNDER, peri, null);
		assertFalse(poss.toString(), poss.get());
		assertEquals(IRejectReasons.IMPOSS_NOT_EMBEDDABLE, poss.getI18NKey().get(0).getKey());
		// Embedding something in an existing slot, not wanted by embedded item
		poss = ctrl.canBeEmbedded(container, ItemHook.TOP, bipod, null);
		assertFalse(poss.toString(), poss.get());
		assertEquals(IRejectReasons.IMPOSS_NOT_EMBEDDABLE, poss.getI18NKey().get(0).getKey());

		res = ctrl.embed(container, ItemHook.TOP, peri, null);
		assertTrue(res.wasSuccessful());
		assertNotNull(res.get());
		// Nuyen should be 340 - 70
		assertEquals("Wrong nuyen paid", 270, model.getNuyen());

		assertFalse("Item not in slot after embedding", container.getSlot(ItemHook.TOP).getAllEmbeddedItems().isEmpty());
	}

	//-------------------------------------------------------------------
	/**
	 * A Body 0 microdrone has no electronics mod slots (Double Clutch p.121),
	 * but anti-theft rating 2 needs none, while rating 3 needs one (p.140).
	 */
	@Test
	public void testEmbeddingAntiTheftInMicrodrone() {
		preMods.add(new ValueModification(ShadowrunReference.CREATION_POINTS, CreatePoints.NUYEN.name(), 10000));
		charGen.runProcessors();
		OperationResult<CarriedItem<ItemTemplate>> res = ctrl.select(Shadowrun6Core.getItem(ItemTemplate.class, "gmc_micromachine"));
		assertTrue(res.wasSuccessful());
		CarriedItem<ItemTemplate> drone = res.get();
		assertEquals("Default anti-theft missing", 1, drone.getAccessory("anti_theft", "rating1").size());

		ItemTemplate theft = Shadowrun6Core.getItem(ItemTemplate.class, "anti_theft");
		Possible poss = ctrl.canBeEmbedded(drone, ItemHook.VEHICLE_ELECTRONICS, theft, "rating2");
		assertTrue(poss.toString(), poss.get());
		poss = ctrl.canBeEmbedded(drone, ItemHook.VEHICLE_ELECTRONICS, theft, "rating3");
		assertFalse(poss.toString(), poss.get());
		assertEquals(IRejectReasons.IMPOSS_CAPACITY, poss.getI18NKey().get(0).getKey());
	}

	//-------------------------------------------------------------------
	@Test
	public void loadSoftware() {
		ItemTemplate item = Shadowrun6Core.getItem(ItemTemplate.class, "maersk_spider");
		ItemTemplate needle = Shadowrun6Core.getItem(ItemTemplate.class, "targeting");

		// New create an item
		OperationResult<CarriedItem<ItemTemplate>> result = GearTool.buildItem(item, CarryMode.CARRIED, null, true);
		assertTrue(result.isPresent());
		CarriedItem<ItemTemplate> carried = result.get();
		assertNotNull("CarriedItem not created",carried);

		preMods.add(new ValueModification(ShadowrunReference.CREATION_POINTS, CreatePoints.NUYEN.name(), 1500));
		charGen.runProcessors();
		Possible poss = ctrl.canBeEmbedded(result.get(), ItemHook.SOFTWARE, needle, null,
				new Decision(ItemTemplate.UUID_RATING, "3"),
				new Decision(UUID.fromString("2baf4c6e-417b-4d1a-943c-edfa816d50bf"), "ares_predator_vi")
				);
		assertTrue(poss.toString(),poss.get());

		// Check that it can be assigned to library as well
		// ResetModifications creates SoftwareLibrary
		Shadowrun6Tools.runProcessors(model, Locale.getDefault());
		assertNotNull("No virtual software library",model.getSoftwareLibrary());
		poss = ctrl.canBeEmbedded(model.getSoftwareLibrary(), ItemHook.SOFTWARE, needle, null,
				new Decision(ItemTemplate.UUID_RATING, "3"),
				new Decision(UUID.fromString("2baf4c6e-417b-4d1a-943c-edfa816d50bf"), "ares_predator_vi")
				);
		assertTrue(poss.toString(),poss.get());
	}

	//-------------------------------------------------------------------
	@Test
	public void loadSoftwareTypes() {
		// Set enough nuyen
		preMods.add(new ValueModification(ShadowrunReference.CREATION_POINTS, CreatePoints.NUYEN.name(), 60000));
		charGen.runProcessors();

		ItemTemplate contRaw = Shadowrun6Core.getItem(ItemTemplate.class, "allegiance_control_center");
		ItemTemplate itemRaw = Shadowrun6Core.getItem(ItemTemplate.class, "browse");

		// Prepare container
		OperationResult<CarriedItem<ItemTemplate>> result = ctrl.select(contRaw, null, CarryMode.CARRIED);
		assertTrue(result.isPresent());
		CarriedItem<ItemTemplate> container = result.get();

		// Test wrong variant
		Possible poss = ctrl.canBeEmbedded(container, ItemHook.SOFTWARE, itemRaw, "basic");
		assertFalse("Should not be possible to embed 'basic' variant in typed slot: "+poss, poss.get());

		// Test correct variant
		poss = ctrl.canBeEmbedded(container, ItemHook.SOFTWARE, itemRaw, "rcc");
		assertTrue("Should be possible to embed 'rcc' variant in typed slot: "+poss+"/"+poss.getUnfulfilledRequirements(), poss.get());
	}

	//-------------------------------------------------------------------
	@Test
	public void testCyberadept() {
		ItemTemplate item = Shadowrun6Core.getItem(ItemTemplate.class, "datajack");
		assertNotNull(item);
		CarriedItem<ItemTemplate> carried = SR6GearTool.buildItem(item, CarryMode.IMPLANTED, model, true,
				new Decision(ItemTemplate.CHOICE_AUGMENTATION_QUALITY, AugmentationQuality.STANDARD.name())).get();
		assertEquals(AugmentationQuality.STANDARD, carried.getAsObject(SR6ItemAttribute.QUALITY).getModifiedValue());

//		DataItemModification itemMod = new DataItemModification(ShadowrunReference.AUGMENTATION_QUALITY, AugmentationQuality.ALPHA.name());
//		carried.addModification(itemMod);
		ValueModification valMod = new ValueModification(ShadowrunReference.ITEM_ATTRIBUTE, SR6ItemAttribute.QUALITY.name(), "ALPHA", AugmentationQuality.ALPHA);
		carried.getAsObject(SR6ItemAttribute.QUALITY).addIncomingModification(valMod);
		assertEquals(AugmentationQuality.ALPHA, carried.getAsObject(SR6ItemAttribute.QUALITY).getModifiedValue());
		ValueModification valMod2 = new ValueModification(ShadowrunReference.ITEM_ATTRIBUTE, SR6ItemAttribute.QUALITY.name(), "DELTA", AugmentationQuality.DELTA);
		carried.getAsObject(SR6ItemAttribute.QUALITY).addIncomingModification(valMod2);
		assertEquals(AugmentationQuality.DELTA, carried.getAsObject(SR6ItemAttribute.QUALITY).getModifiedValue());

//		SR6GearTool.recalculate("", model, carried);
		System.out.println(carried.dump());

		carried.reset();
		System.out.println(carried.dump());
	}

}
