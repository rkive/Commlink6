package org.prelle.rpgframework.shadowrun6.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.prelle.simplepersist.Persister;

import de.rpgframework.genericrpg.chargen.OperationResult;
import de.rpgframework.genericrpg.data.ApplyTo;
import de.rpgframework.genericrpg.data.Decision;
import de.rpgframework.genericrpg.data.SkillSpecialization;
import de.rpgframework.genericrpg.items.CarriedItem;
import de.rpgframework.genericrpg.items.CarryMode;
import de.rpgframework.genericrpg.items.GearTool;
import de.rpgframework.genericrpg.items.ItemAttributeDefinition;
import de.rpgframework.genericrpg.items.ItemAttributeFloatValue;
import de.rpgframework.genericrpg.items.ItemEnhancementValue;
import de.rpgframework.genericrpg.modification.Modification;
import de.rpgframework.genericrpg.modification.ValueModification;
import de.rpgframework.shadowrun.DamageElement;
import de.rpgframework.shadowrun.DamageType;
import de.rpgframework.shadowrun.ShadowrunAttribute;
import de.rpgframework.shadowrun.items.AugmentationQuality;
import de.rpgframework.shadowrun.items.Availability;
import de.rpgframework.shadowrun.items.Legality;
import de.rpgframework.shadowrun6.SR6Skill;
import de.rpgframework.shadowrun6.Shadowrun6Character;
import de.rpgframework.shadowrun6.Shadowrun6Core;
import de.rpgframework.shadowrun6.data.Shadowrun6DataPlugin;
import de.rpgframework.shadowrun6.items.AvailableSlot;
import de.rpgframework.shadowrun6.items.Damage;
import de.rpgframework.shadowrun6.items.ItemHook;
import de.rpgframework.shadowrun6.items.ItemSubType;
import de.rpgframework.shadowrun6.items.ItemTemplate;
import de.rpgframework.shadowrun6.items.ItemType;
import de.rpgframework.shadowrun6.items.SR6GearTool;
import de.rpgframework.shadowrun6.items.SR6ItemAttribute;
import de.rpgframework.shadowrun6.items.SR6ItemEnhancement;
import de.rpgframework.shadowrun6.items.SR6PieceOfGearVariant;
import de.rpgframework.shadowrun6.modifications.ShadowrunReference;
import de.rpgframework.shadowrun6.proc.CalculateMeleeAndUnarmed;

/**
 * @author prelle
 *
 */
public class SR6CarriedItemTest {

	//-------------------------------------------------------------------
	@BeforeClass
	public static void beforeClass() {
		System.setProperty("logdir", "/tmp");
		Locale.setDefault(Locale.ENGLISH);
		Shadowrun6DataPlugin plugin = new Shadowrun6DataPlugin();
		plugin.init( );
	}

	//-------------------------------------------------------------------
	/**
	 * 	<item id="combat_axe" avail="4" price="500" type="WEAPON_CLOSE_COMBAT" subtype="BLADES">
	 *	  <equip mode="NORMAL"/>
	 *	  <weapon dmg="5P" attack="9,,,," skill="close_combat" spec="close_combat/blades" />
	 *  </item>
	 */
	@Test
	public void testSimpleItem() {
		ItemTemplate axe = Shadowrun6Core.getItem(ItemTemplate.class, "combat_axe");
		assertNotNull(axe);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(axe, null, CarryMode.CARRIED);
		assertNotNull(item);
		OperationResult<List<Modification>> modR = SR6GearTool.recalculate("", null, item);
		assertTrue(modR.wasSuccessful());
		assertNotNull(item.getAsObject(SR6ItemAttribute.AVAILABILITY));
		assertEquals(4, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getValue());
		assertEquals(Legality.LEGAL, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getLegality());
		assertNotNull(item.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(500, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());
		assertNotNull(item.getAsObject(SR6ItemAttribute.DAMAGE));
		assertEquals(5, ((Damage)item.getAsObject(SR6ItemAttribute.DAMAGE).getModifiedValue()).getValue());
		assertEquals(DamageType.PHYSICAL, ((Damage)item.getAsObject(SR6ItemAttribute.DAMAGE).getModifiedValue()).getType());
		assertNotNull(item.getAsObject(SR6ItemAttribute.ATTACK_RATING));
		assertArrayEquals(new int[]{9,0,0,0,0}, (int[])item.getAsObject(SR6ItemAttribute.ATTACK_RATING).getModifiedValue());
		assertNotNull(item.getAsObject(SR6ItemAttribute.SKILL));
		assertEquals(Shadowrun6Core.getSkill("close_combat"), (SR6Skill)item.getAsObject(SR6ItemAttribute.SKILL).getModifiedValue());
		assertNotNull(item.getAsObject(SR6ItemAttribute.SKILL_SPECIALIZATION));
		assertEquals(Shadowrun6Core.getSkill("close_combat").getSpecialization("blades"), (SkillSpecialization<SR6Skill>)item.getAsObject(SR6ItemAttribute.SKILL_SPECIALIZATION).getModifiedValue());
	}

	//-------------------------------------------------------------------
	/**
	 * <item id="bow" price="100" type="WEAPON_RANGED" subtype="BOWS">
	 * 	<choices>
	 * 		<choice uuid="adeb159c-6ca3-407b-8641-c76f9b29a49c" type="ITEM_ATTRIBUTE" ref="RATING" options="1,2,3,4,5,6,7,8,9,10,11,12,13,14"/>
	 * 	</choices>
	 * 	<requires>
	 * 		<valuereq type="ATTRIBUTE" ref="STRENGTH" min="$RATING"/>
	 * 	</requires>
     *   <modifications>
     *      <itemmod type="SLOT" ref="TOP"/>
     *      <itemmod type="SLOT" ref="UNDER"/>
     *   </modifications>
	 * 	<attrdef id="PRICE"         value="$RATING*10 +100" />
	 * 	<attrdef id="DAMAGE"        value="$RATING/2 P" />
	 * 	<attrdef id="ATTACK_RATING" value="$RATING/2,$RATING,$RATING/4,," />
	 * 	<attrdef id="AVAILABILITY"  value="$RATING/3 L" />
	 * 	<weapon skill="athletics" spec="athletics/archery" />
	 * </item>
	 */
	@Test
	public void testItemWithRating() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "bow");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		Decision decision = new Decision(UUID.fromString("c2d17c87-1cfe-4355-9877-a20fe09c170d"), "9");
		item.setDecisions(List.of(decision));
		OperationResult<List<Modification>> modR = SR6GearTool.recalculate("", null, item);
		assertTrue(modR.wasSuccessful());

		assertNotNull(item.getAsObject(SR6ItemAttribute.AVAILABILITY));
		assertEquals(3, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getValue());
		assertEquals(Legality.RESTRICTED, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getLegality());
		assertNotNull("No PRICE set",item.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(190, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());
		assertNotNull(item.getAsObject(SR6ItemAttribute.DAMAGE));
		assertEquals(5, ((Damage)item.getAsObject(SR6ItemAttribute.DAMAGE).getModifiedValue()).getValue());
		assertEquals(DamageType.PHYSICAL, ((Damage)item.getAsObject(SR6ItemAttribute.DAMAGE).getModifiedValue()).getType());
		assertNotNull(item.getAsObject(SR6ItemAttribute.ATTACK_RATING));
		assertArrayEquals(new int[]{5,9,2,0,0}, (int[])item.getAsObject(SR6ItemAttribute.ATTACK_RATING).getModifiedValue());
		assertNotNull(item.getAsObject(SR6ItemAttribute.SKILL));
		assertEquals(Shadowrun6Core.getSkill("athletics"), (SR6Skill)item.getAsObject(SR6ItemAttribute.SKILL).getModifiedValue());
		assertNotNull(item.getAsObject(SR6ItemAttribute.SKILL_SPECIALIZATION));
		assertEquals(Shadowrun6Core.getSkill("athletics").getSpecialization("archery"), (SkillSpecialization<SR6Skill>)item.getAsObject(SR6ItemAttribute.SKILL_SPECIALIZATION).getModifiedValue());
	}

	//-------------------------------------------------------------------
	@Test
	public void testItemWithRatingAndModifications() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "muscle_toner");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, null, CarryMode.IMPLANTED);
		Decision dec1 = new Decision(ItemTemplate.CHOICE_AUGMENTATION_QUALITY, "STANDARD");
		Decision dec2 = new Decision(UUID.fromString("c2d17c87-1cfe-4355-9877-a20fe09c170d"), "3");
		item.setDecisions(List.of(dec1, dec2));
		OperationResult<List<Modification>> modR = SR6GearTool.recalculate("", null, item);
		assertTrue(modR.wasSuccessful());

		assertNotNull(item.getAsObject(SR6ItemAttribute.AVAILABILITY));
		assertEquals(4, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getValue());
		assertEquals(Legality.RESTRICTED, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getLegality());
		assertNotNull("No PRICE set",item.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(96000, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());

		// Now validate modifications
		assertNotNull(item.getIncomingModifications());
		assertNotNull(item.getOutgoingModifications());
		assertTrue("Muscle toner should only have character modifications",item.getIncomingModifications().isEmpty());
		assertFalse("Muscle toner should have character modifications",item.getOutgoingModifications().isEmpty());

		Modification cMod = item.getOutgoingModifications().get(0);
		ValueModification vMod = (ValueModification)cMod;
		assertEquals(ShadowrunReference.ATTRIBUTE,vMod.getReferenceType());
		assertEquals(ApplyTo.CHARACTER, vMod.getApplyTo());
		assertEquals(ShadowrunAttribute.AGILITY, vMod.getResolvedKey());
		assertNotNull(vMod.getFormula());
		assertEquals(3,vMod.getValue());
//		assertTrue("Formula not resolved",vMod.getFormula().isResolved());
//		assertEquals(3,vMod.getFormula().getAsInteger());
	}

	//-------------------------------------------------------------------
	/**
     * <item id="defiance_super_shock" avail="1" price="340" type="WEAPON_FIREARMS" subtype="TASERS">
     *    <modifications>
     *       <itemmod type="HOOK" ref="TOP"/>
     *    </modifications>
     *    <weapon dmg="6S(e)" attack="10,6,,," mode="SS" ammo="4(m)" skill="firearms" spec="firearms/pistols"/>
     * </item>
	 */
	@Test
	public void testItemWithSlots() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "defiance_super_shock");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		SR6GearTool.recalculate("", null, item);
		assertNotNull(item);
		assertNotNull(item.getAsObject(SR6ItemAttribute.AVAILABILITY));
		assertEquals(1, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getValue());
		assertEquals(Legality.LEGAL, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getLegality());
		assertNotNull(item.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(340, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());
		assertNotNull(item.getAsObject(SR6ItemAttribute.DAMAGE));
		assertEquals(6, ((Damage)item.getAsObject(SR6ItemAttribute.DAMAGE).getModifiedValue()).getValue());
		assertEquals(DamageType.STUN, ((Damage)item.getAsObject(SR6ItemAttribute.DAMAGE).getModifiedValue()).getType());
		assertEquals(DamageElement.ELECTRICITY, ((Damage)item.getAsObject(SR6ItemAttribute.DAMAGE).getModifiedValue()).getElement());
		assertNotNull(item.getAsObject(SR6ItemAttribute.ATTACK_RATING));
		assertArrayEquals(new int[]{10,6,0,0,0}, (int[])item.getAsObject(SR6ItemAttribute.ATTACK_RATING).getModifiedValue());
		assertNotNull(item.getAsObject(SR6ItemAttribute.SKILL));
		assertEquals(Shadowrun6Core.getSkill("firearms"), (SR6Skill)item.getAsObject(SR6ItemAttribute.SKILL).getModifiedValue());
		assertNotNull(item.getAsObject(SR6ItemAttribute.SKILL_SPECIALIZATION));
		assertEquals(Shadowrun6Core.getSkill("firearms").getSpecialization("tasers"), (SkillSpecialization<SR6Skill>)item.getAsObject(SR6ItemAttribute.SKILL_SPECIALIZATION).getModifiedValue());

		//item.get
	}

	//-------------------------------------------------------------------
	@Test
	public void loadItemWithSlots() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "transys_avalon");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		SR6GearTool.recalculate("", null, item);
		assertNotNull(item);

		assertNotNull("Item misses slot", item.getSlot(ItemHook.ELECTRONIC_ACCESSORY));
	}

	//-------------------------------------------------------------------
	@Test
	public void testCapacityFromSlots() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "cyberarm");
		assertNotNull(temp);
		SR6PieceOfGearVariant variant = (SR6PieceOfGearVariant) temp.getVariant("forearm_obvious");

		OperationResult<CarriedItem<ItemTemplate>> res = SR6GearTool.buildItem(temp, CarryMode.IMPLANTED, variant, null, false, new Decision(ItemTemplate.CHOICE_AUGMENTATION_QUALITY, "STANDARD"));
		assertNotNull(res);
		CarriedItem<ItemTemplate> item = res.get();
		assertNotNull(item);
		assertNull(item.getUsedSlot());
		assertNotNull(item.getAsObject(SR6ItemAttribute.AVAILABILITY));
		assertEquals(3, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getValue());
		assertEquals(Legality.LEGAL, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getLegality());
		assertNotNull(item.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(10000, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());

		AvailableSlot slot = item.getSlot(ItemHook.CYBERLIMB_IMPLANT);
		assertNotNull("Missing slot",slot);
		assertEquals(10, slot.getCapacity(), 0);
	}

	//-------------------------------------------------------------------
	@Test
	public void testEssence() {
		ItemAttributeFloatValue<SR6ItemAttribute> essenceAttr = new ItemAttributeFloatValue<SR6ItemAttribute>(SR6ItemAttribute.ESSENCECOST, 0.6f);
		ValueModification mod = new ValueModification(
				ShadowrunReference.ITEM_ATTRIBUTE,
				SR6ItemAttribute.ESSENCECOST.name(),
				Math.round(essenceAttr.getDistributed()*-300),
				AugmentationQuality.BETA
				);
		essenceAttr.addIncomingModification(mod);
		assertEquals(0.42f, essenceAttr.getModifiedValue(), 0f);

		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "reaction_enhancers");
		assertNotNull(temp);

		OperationResult<CarriedItem<ItemTemplate>> res = SR6GearTool.buildItem(temp, CarryMode.IMPLANTED, null, null, false,
				new Decision(temp.getChoice("RATING"), "2"),
				new Decision(ItemTemplate.CHOICE_AUGMENTATION_QUALITY, "BETA")
				);
		assertNotNull(res);
		CarriedItem<ItemTemplate> item = res.get();
		assertNotNull(item);
		assertNotNull(item.getAsObject(SR6ItemAttribute.AVAILABILITY));
		// Unmodified for Betaware, availability would be 4, but 6 is correct
		assertEquals(6, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getValue());
		assertEquals(Legality.RESTRICTED, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getLegality());
		assertNotNull(item.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(45000, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());
		assertNotNull("ESSENCE not calculated",item.getAsFloat(SR6ItemAttribute.ESSENCECOST));
		assertEquals(0.42f, item.getAsFloat(SR6ItemAttribute.ESSENCECOST).getModifiedValue(), 0.0f);
	}

	//-------------------------------------------------------------------
	/**
	 * 	<item id="combat_axe" avail="4" price="500" type="WEAPON_CLOSE_COMBAT" subtype="BLADES">
	 *	  <equip mode="NORMAL"/>
	 *	  <weapon dmg="5P" attack="9,,,," skill="close_combat" spec="close_combat/blades" />
	 *  </item>
	 */
	@Test
	public void testVehicle() {
		ItemTemplate honda = Shadowrun6Core.getItem(ItemTemplate.class, "honda_spirit");
		assertNotNull(honda);
		ItemAttributeDefinition def = honda.getAttribute(SR6ItemAttribute.BODY);
		assertNotNull(def);
		assertTrue("BODY should be an integer type",def.isInteger());

		CarriedItem<ItemTemplate> item = GearTool.buildItem(honda, CarryMode.CARRIED, null, true).get();
		assertNotNull(item);
		assertNotNull(item.getAsObject(SR6ItemAttribute.AVAILABILITY));
		assertEquals(2, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getValue());
		assertEquals(ItemType.VEHICLES, item.getAsObject(SR6ItemAttribute.ITEMTYPE).getValue());
		assertEquals(ItemSubType.CARS, item.getAsObject(SR6ItemAttribute.ITEMSUBTYPE).getValue());
		assertEquals(Legality.LEGAL, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getLegality());
		assertNotNull(item.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(13000, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());
		assertEquals(10, item.getAsValue(SR6ItemAttribute.BODY).getModifiedValue());
	}

	//-------------------------------------------------------------------
	@Test
	public void testVehicleAntiTheft() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "chrysler-nissan_jackrabbit");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> vehicle = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		List<CarriedItem<ItemTemplate>> thefts = vehicle.getAccessory("anti_theft", "rating1");
		assertTrue(thefts.isEmpty());
		SR6GearTool.recalculate("", null, vehicle);
		thefts = vehicle.getAccessory("anti_theft", "rating1");
//		System.out.println(vehicle.dump());
		assertFalse("Anti-Theft missing",thefts.isEmpty());
		assertEquals(1,thefts.size());
		SR6GearTool.recalculate("", null, vehicle);
		SR6GearTool.recalculate("", null, vehicle);
		thefts = vehicle.getAccessory("anti_theft", "rating1");
		System.out.println(vehicle.dump());
		assertFalse("Anti-Theft missing",thefts.isEmpty());
		assertEquals("Thefts added multiple times",1,thefts.size());
	}

	//-------------------------------------------------------------------
	/**
	 * Every vehicle with a Pilot rating has a rating 1 anti-theft system
	 * (Double Clutch p.139), including Body 0 microdrones, which have no
	 * electronics mod slots. Rating 1 and 2 need no slots, rating 3 needs one.
	 */
	@Test
	public void testMicrodroneAntiTheft() {
		for (String id : List.of("gmc_micromachine", "shiawase_kanmushi", "mct-sikorsky-bell_microskimmer_xxs")) {
			ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, id);
			assertNotNull(temp);

			CarriedItem<ItemTemplate> drone = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
			SR6GearTool.recalculate("", null, drone);
			SR6GearTool.recalculate("", null, drone);
			List<CarriedItem<ItemTemplate>> thefts = drone.getAccessory("anti_theft", "rating1");
			assertEquals("Anti-Theft missing or duplicated on "+id,1,thefts.size());

			AvailableSlot electronics = drone.getSlot(ItemHook.VEHICLE_ELECTRONICS);
			assertNotNull("No electronics slot on "+id,electronics);
			assertEquals(0f, electronics.getCapacity(), 0f);
			assertEquals(0f, electronics.getFreeCapacity(), 0f);
		}
	}

	//-------------------------------------------------------------------
	@Test
	public void testWithoutChoices() {
		ItemTemplate honda = Shadowrun6Core.getItem(ItemTemplate.class, "dermal_plating");
		assertNotNull(honda);

		CarriedItem<ItemTemplate> item = SR6GearTool.buildItem(honda, CarryMode.IMPLANTED, null, false).get();
		assertNotNull(item);
		assertNotNull(item.getAsObject(SR6ItemAttribute.AVAILABILITY));
		assertNotNull(item.getAsValue(SR6ItemAttribute.PRICE));
	}

	//-------------------------------------------------------------------
	@Test
	public void testItemWithCharMods() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "bone_lacing");
		assertNotNull(temp);
		assertEquals("No. modifications wrong in ItemTemplate.",1, temp.getOutgoingModifications().size());

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, temp.getVariant("aluminium"), CarryMode.IMPLANTED);
		SR6GearTool.recalculate("", null, item);
		assertEquals("No. modifications wrong in CI.",0, item.getIncomingModifications().size());
		assertEquals("No. char modifications wrong in CI.",5, item.getOutgoingModifications().size());
		Decision decision = new Decision(ItemTemplate.UUID_AUGMENTATION_QUALITY, AugmentationQuality.STANDARD.name());
		item.setDecisions(List.of(decision));
		assertEquals("No. modifications wrong in CI.",5, item.getOutgoingModifications().size());
		OperationResult<List<Modification>> modR = SR6GearTool.recalculate("", null, item);
		assertEquals("No. modifications wrong in CI.",5, item.getOutgoingModifications().size());
		assertTrue(modR.wasSuccessful());
		assertFalse(item.getOutgoingModifications().isEmpty());
//		assertTrue( item.getOutgoingModifications().contains( new DataItemModification(ShadowrunReference.RULE, SR6RuleFlag.UNARMED_DAMAGE_IS_PHYSICAL.name())));
//		assertTrue( item.getOutgoingModifications().contains( new DataItemModification(ShadowrunReference.ATTRIBUTE, ShadowrunAttribute.RESIST_DAMAGE.name())));
//		assertEquals("No. modifications wrong.",5, modR.get().size());

		assertNotNull(item.getAsObject(SR6ItemAttribute.AVAILABILITY));
		assertEquals(4, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getValue());
		assertEquals(Legality.RESTRICTED, ((Availability)item.getAsObject(SR6ItemAttribute.AVAILABILITY).getModifiedValue()).getLegality());
		assertNotNull("No PRICE set",item.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(18000, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());
	}

	//-------------------------------------------------------------------
	@Test
	public void testAmmunition() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "ammo_holdout_light_machine");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, temp.getVariant("caseless"), CarryMode.CARRIED);
		item.addDecision(new Decision(UUID.fromString("b015341d-24dc-42bb-a46b-781a5340e0b3"),"apds"));
		SR6GearTool.recalculate("", null, item);

		assertNotNull("No PRICE set",item.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(30, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());
	}

	//-------------------------------------------------------------------
	private static int getArtifical(ShadowrunAttribute key, List<Modification> mods) {
		int sum = 0;
		for (Modification mod : mods) {
			if (mod.getReferenceType()!=ShadowrunReference.ATTRIBUTE)
				continue;
			ValueModification vMod = (ValueModification)mod;
			if (vMod.getResolvedKey()!=key)
				continue;
			sum += vMod.getValue();
		}
		return sum;
	}

	//-------------------------------------------------------------------
	@Test
	public void testCyberlimb() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "cyberarm");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, temp.getVariant("fullarm_obvious"), CarryMode.IMPLANTED);
		item.addDecision(new Decision(ItemTemplate.UUID_AUGMENTATION_QUALITY, AugmentationQuality.STANDARD.name()));
		SR6GearTool.recalculate("", null, item);

		assertNotNull("No PRICE set",item.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(15000, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());

		assertEquals(3, item.getOutgoingModifications().size() );
		// Artificial strength should be 2
		assertEquals(2, getArtifical(ShadowrunAttribute.STRENGTH, item.getOutgoingModifications()));
		assertEquals(2, getArtifical(ShadowrunAttribute.AGILITY, item.getOutgoingModifications()));

		// Increase STR
		CarriedItem<ItemTemplate> incSTR = new CarriedItem<ItemTemplate>(Shadowrun6Core.getItem(ItemTemplate.class, "attribute_increase"), null, CarryMode.EMBEDDED);
		incSTR.addDecision(new Decision(ItemTemplate.UUID_AUGMENTATION_QUALITY, AugmentationQuality.STANDARD.name()));
		incSTR.addDecision(new Decision(ItemTemplate.UUID_RATING, "4"));
		incSTR.addDecision(new Decision(UUID.fromString("d5c88f1f-eb6f-4057-b9d0-b65c212747e6"), "STRENGTH"));
		SR6GearTool.recalculate("", null, incSTR);
		assertEquals(4, getArtifical(ShadowrunAttribute.STRENGTH, incSTR.getOutgoingModifications()));

		item.addAccessory(incSTR, ItemHook.CYBERLIMB_IMPLANT);
		System.out.println("\n\n--------Recalculate----------");
		SR6GearTool.recalculate("", null, item);
		System.out.println(item.dump());
		for (Modification mod : item.getOutgoingModifications()) {
			System.out.println("OUT "+mod);
		}
		// Artificial strength should be 6
		assertEquals(6, getArtifical(ShadowrunAttribute.STRENGTH, item.getOutgoingModifications()));
	}

	//-------------------------------------------------------------------
	@Test
	public void testCyberlimbIncrease() {
		CarriedItem<ItemTemplate> incSTR = new CarriedItem<ItemTemplate>(Shadowrun6Core.getItem(ItemTemplate.class, "attribute_increase"), null, CarryMode.EMBEDDED);
		incSTR.addDecision(new Decision(ItemTemplate.UUID_AUGMENTATION_QUALITY, AugmentationQuality.STANDARD.name()));
		incSTR.addDecision(new Decision(ItemTemplate.UUID_RATING, "4"));
		incSTR.addDecision(new Decision(UUID.fromString("d5c88f1f-eb6f-4057-b9d0-b65c212747e6"), "STRENGTH"));
		SR6GearTool.recalculate("", null, incSTR);
		assertEquals(4, getArtifical(ShadowrunAttribute.STRENGTH, incSTR.getOutgoingModifications()));
	}

	//-------------------------------------------------------------------
	@Test
	public void testModes() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "ares_predator_vi");
		ItemTemplate acc  = Shadowrun6Core.getItem(ItemTemplate.class, "laser_sight");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		SR6GearTool.recalculate("", null, item);
		assertNotNull(item);
		assertNotNull("Modes missing",item.getOperationModes(true));
		assertTrue("Should not have more than WIRELESS yet", item.getOperationModes(true).size()<2);

		CarriedItem<ItemTemplate> accItem = new CarriedItem<ItemTemplate>(acc, null, CarryMode.EMBEDDED);
		SR6GearTool.recalculate("", null, accItem);
		assertFalse("Should have modes", accItem.getOperationModes(true).isEmpty());

		item.addAccessory(accItem, ItemHook.TOP);
		SR6GearTool.recalculate("", null, item);
		assertNotNull("Modes missing",item.getOperationModes(true));
		System.out.println("SR6CarriedItemTest.testModes: "+item.getOperationModes(true));
		assertFalse("Should have 2+ modes now, but was "+item.getOperationModes(true).get(0).getModes().size(), item.getOperationModes(true).get(0).getModes().size()<2);

	}

	//-------------------------------------------------------------------
	@Test
	public void testModifications() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "ares_predator_vi");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		SR6GearTool.recalculate("", null, item);
		assertNotNull(item);
		assertNotNull(item.getAsObject(SR6ItemAttribute.ATTACK_RATING));
		int[] expect = new int[] {10,10,8,0,0};
		int[] real   = (int[])item.getAsObject(SR6ItemAttribute.ATTACK_RATING).getModifiedValue();
		assertTrue("Expect "+Arrays.toString(expect)+" but got "+Arrays.toString(real), Arrays.equals(expect, real));
		assertNotNull(item.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(750, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());

		// Add modification
		SR6ItemEnhancement foregrip = Shadowrun6Core.getItem(SR6ItemEnhancement.class, "foregrip");
		assertNotNull(foregrip);
		item.addEnhancement(new ItemEnhancementValue<SR6ItemEnhancement>(foregrip));
		SR6GearTool.recalculate("", null, item);
		expect = new int[] {11,12,9,0,0};
		real   = (int[])item.getAsObject(SR6ItemAttribute.ATTACK_RATING).getModifiedValue();
		assertTrue("Expect "+Arrays.toString(expect)+" but got "+Arrays.toString(real), Arrays.equals(expect, real));
		assertEquals(1000, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());
	}

	//-------------------------------------------------------------------
	@Test
	public void testChemical() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "gas_grenade");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		item.addDecision(new Decision(ItemTemplate.UUID_CHEMICAL_CHOICE, "neuro_stun_x"));
		SR6GearTool.recalculate("", null, item);
		assertNotNull(item);
		assertNotNull(item.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(2050, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());
	}

	//-------------------------------------------------------------------
	@Test
	public void testRuleMeleeHardening() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "ares_light_fire_70");
		assertNotNull(temp);
		SR6ItemEnhancement harden = Shadowrun6Core.getItem(SR6ItemEnhancement.class, "melee_hardening");
		assertNotNull(harden);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		// Add melee hardening modification
		item.addEnhancement(new ItemEnhancementValue<SR6ItemEnhancement>(harden));
		SR6GearTool.recalculate("", null, item);
		assertNotNull(item);
		assertTrue("No alternates found",item.getAlternates().size()>0);
	}

	//-------------------------------------------------------------------
	@Test
	public void testVariableCostAccessories() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "yamaha_growler");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> yamaha = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		SR6GearTool.recalculate("", null, yamaha);
		assertNotNull(yamaha);
		assertNotNull(yamaha.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(8000, yamaha.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());

		ItemTemplate accessory = Shadowrun6Core.getItem(ItemTemplate.class, "realistic_features");
		assertNotNull(accessory);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(accessory, null, CarryMode.CARRIED);
		item.addDecision(new Decision(ItemTemplate.UUID_RATING, "3"));
		SR6GearTool.recalculate("", null, item);
		assertNotNull(item);
		// Item has not been added yet - there should be no price
		assertNull(item.getAsValue(SR6ItemAttribute.PRICE));

		// Now add it to the Yamaha
		yamaha.addAccessory(item, ItemHook.VEHICLE_ACCESSORY);
		SR6GearTool.recalculate("", null, item);

		assertNotNull(item.getAsValue(SR6ItemAttribute.PRICE));
		// Expected price is 1000 * Rating(3) * Body(6)
		assertEquals(18000, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());
	}

	//-------------------------------------------------------------------
	@Test
	public void testVariableCostAccessories2() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "chrysler-nissan_jackrabbit");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> yamaha = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		SR6GearTool.recalculate("", null, yamaha);
		assertNotNull(yamaha);
		assertNotNull(yamaha.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(11000, yamaha.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());
		assertEquals(8, yamaha.getSlot(ItemHook.VEHICLE_CHASSIS).getFreeCapacity(), 0);
		assertEquals(8, yamaha.getSlot(ItemHook.VEHICLE_CHASSIS).getCapacity(), 0);
		assertEquals(0, yamaha.getSlot(ItemHook.VEHICLE_CHASSIS).getUsedCapacity(), 0);

		ItemTemplate accessory = Shadowrun6Core.getItem(ItemTemplate.class, "easy_assembly");
		assertNotNull(accessory);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(accessory, null, CarryMode.EMBEDDED);
		SR6GearTool.recalculate("", null, item);
		assertNotNull(item);
		assertEquals(2000, item.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());

		// Now add it to the Yamaha
		yamaha.addAccessory(item, ItemHook.VEHICLE_CHASSIS);
		SR6GearTool.recalculate("", null, item);
		System.err.println("----------recalc-------");
		SR6GearTool.recalculate("", null, yamaha);

		assertNotNull(item.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(13000, yamaha.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());
		// Expected price is 1000 * Rating(3) * Body(6)
		assertEquals(5, yamaha.getSlot(ItemHook.VEHICLE_CHASSIS).getFreeCapacity(), 0);
		assertEquals(8, yamaha.getSlot(ItemHook.VEHICLE_CHASSIS).getCapacity(), 0);
		assertEquals(3, yamaha.getSlot(ItemHook.VEHICLE_CHASSIS).getUsedCapacity(), 0);
	}

	//-------------------------------------------------------------------
	@Test
	public void testAccessoryGrantingModSlots() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "chrysler-nissan_jackrabbit");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> yamaha = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		SR6GearTool.recalculate("", null, yamaha);
		assertNotNull(yamaha);
		assertNotNull(yamaha.getAsValue(SR6ItemAttribute.PRICE));
		assertEquals(11000, yamaha.getAsValue(SR6ItemAttribute.PRICE).getModifiedValue());
		assertEquals(8, yamaha.getSlot(ItemHook.VEHICLE_CHASSIS).getFreeCapacity(), 0);
		assertEquals(8, yamaha.getSlot(ItemHook.VEHICLE_CHASSIS).getCapacity(), 0);
		assertEquals(0, yamaha.getSlot(ItemHook.VEHICLE_CHASSIS).getUsedCapacity(), 0);

		AvailableSlot slot = yamaha.getSlot(ItemHook.VEHICLE_HARDPOINT);
		assertNotNull(slot);
		assertEquals(2, slot.getCapacity(), 0);

		ItemTemplate accessory = Shadowrun6Core.getItem(ItemTemplate.class, "hardpoint_huge");
		assertNotNull(accessory);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(accessory, null, CarryMode.EMBEDDED);
		assertNotNull(item);
		yamaha.addAccessory(item, ItemHook.VEHICLE_CHASSIS);
		SR6GearTool.recalculate("", null, yamaha);
		slot = yamaha.getSlot(ItemHook.VEHICLE_HARDPOINT);
		assertEquals(5, slot.getCapacity(), 0);

		// Add something to the hardpoint slot
		ItemTemplate mount = Shadowrun6Core.getItem(ItemTemplate.class, "weapon_mount_standard");
		assertNotNull(mount);
		CarriedItem<ItemTemplate> mountItem = new CarriedItem<ItemTemplate>(mount, null, CarryMode.EMBEDDED);
		assertNotNull(mountItem);
		yamaha.addAccessory(mountItem, ItemHook.VEHICLE_HARDPOINT);
		slot = yamaha.getSlot(ItemHook.VEHICLE_HARDPOINT);
		assertEquals(5, slot.getCapacity(), 0);
		assertEquals(1, slot.getUsedCapacity(), 0);
	}


	//-------------------------------------------------------------------
	@Test
	public void testVehicleAccessories() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "ford_lifeline");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> vehicle = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		SR6GearTool.recalculate("", null, vehicle);

		System.out.println("testVehicleAccessories: accessories = "+vehicle.getAccessories());
		System.out.println("testVehicleAccessories: effective accessories = "+vehicle.getEffectiveAccessories());

		AvailableSlot core = vehicle.getSlot(ItemHook.VEHICLE_CHASSIS);
		assertNotNull(core);
		assertFalse(core.getAllEmbeddedItems().isEmpty());
		System.out.println("Chassis items: "+core.getAllEmbeddedItems());
		System.out.println("All accessories: "+vehicle.getAccessories());
		assertEquals("Expect 4 large hardpoints",4,core.getAllEmbeddedItems().size());

		AvailableSlot cf = vehicle.getSlot(ItemHook.VEHICLE_CF);
		assertNotNull(cf);
		assertFalse(cf.getAllEmbeddedItems().isEmpty());
		System.out.println("CF accessories: "+cf.getAllEmbeddedItems());
		assertEquals("3x Valkyrie, Rigger cocoon, ameneties",5,cf.getAllEmbeddedItems().size());

//		SR6GearTool.recalculate("", null, vehicle);
//		AvailableSlot hp = vehicle.getSlot(ItemHook.VEHICLE_HARDPOINT);
//		assertNotNull(hp);
//		assertEquals("7 + 4x2",15, (int)hp.getCapacity());
	}

	//-------------------------------------------------------------------
	@Test
	public void testCybereye() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "cybereye");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		item.addDecision(new Decision(ItemTemplate.UUID_AUGMENTATION_QUALITY, AugmentationQuality.STANDARD.name()));
		item.addDecision(new Decision(ItemTemplate.UUID_RATING, "4"));
		SR6GearTool.recalculate("", null, item);

		AvailableSlot core = item.getSlot(ItemHook.CYBEREYE_IMPLANT);
		assertNotNull(core);
		assertFalse(core.getAllEmbeddedItems().isEmpty());
		assertEquals("Table-Attribute of valmod not used",12,core.getFreeCapacity(),0);
	}

	//-------------------------------------------------------------------
	@Test
	public void testFactoryWeaponMounts() {
		ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, "nissan_samurai");
		assertNotNull(temp);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, null, CarryMode.CARRIED);
		System.out.println("----Recalculate 1----");
		SR6GearTool.recalculate("", null, item);
		System.out.println("----Recalculate 1 done----");
		System.out.println(item.dump());


		AvailableSlot core = item.getSlot(ItemHook.VEHICLE_HARDPOINT);
		assertNotNull(core);
		assertFalse(core.getAllEmbeddedItems().isEmpty());
		// Make sure there are two weapon mounts
		assertEquals(2,core.getAllEmbeddedItems().stream()
				.filter(acc ->
					acc.getKey().equals("samurai_mount_small"))
				.count());
		// Make sure both have a UUID
		assertEquals("Weapon mounts are missing UUID",2,core.getAllEmbeddedItems().stream()
				.filter(acc -> acc.getKey().equals("samurai_mount_small"))
				.filter(acc -> acc.getUuid()!=null)
				.count());
		// Find the second one
		Optional<CarriedItem<ItemTemplate>> mount = core.getAllEmbeddedItems().stream()
				.filter(acc -> acc.getKey().equals("samurai_mount_small"))
				.filter(acc -> UUID.fromString("0654dd16-b094-4339-b687-72095c446254").equals(acc.getUuid()))
				.findFirst();
		assertTrue("Second weapon mount not found or has wrong UUID",mount.isPresent());

		AvailableSlot smallWeapon = mount.get().getSlot(ItemHook.VEHICLE_WEAPON_SMALL);
		assertNotNull("Weapon mount misses slot",smallWeapon);
		// 4 Items: anti_theft, 2x samurai_mount_small, spurs_samurai_oni
		System.out.println("Effective accessories: "+item.getEffectiveAccessories());
		assertEquals(4, item.getEffectiveAccessories().size());

		// Create a weapon to add
		ItemTemplate wTemp = Shadowrun6Core.getItem(ItemTemplate.class, "walther_palm_pistol");
		CarriedItem<ItemTemplate> wItem = new CarriedItem<ItemTemplate>(wTemp, null, CarryMode.CARRIED);
		SR6GearTool.recalculate("", null, wItem);

		mount.get().addAccessory(wItem, ItemHook.VEHICLE_WEAPON_SMALL);
		System.out.println("Accessories in mount: "+mount.get().getEffectiveAccessories());
		System.out.println("Accessories in item : "+item.getEffectiveAccessories());

		assertEquals(1, smallWeapon.getAllEmbeddedItems().size());
		Collection<CarriedItem<ItemTemplate>> allItems = item.getEffectiveAccessories();
		System.out.println("All items: "+allItems);

		
		Persister persister = new Persister();
		try {
			persister.write(item, System.out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("----Recalculate 2----");
		SR6GearTool.recalculate("", null, item);
		System.out.println("----Recalculate 2 done----");
		allItems = item.getEffectiveAccessories();
		System.err.println("All items: "+allItems);
		assertEquals("Pistol vanished from factory weapon mount",5,allItems.size());
		System.out.println(item.dump());

	}

	//-------------------------------------------------------------------
	@Test
	public void testAlternateUsage() {
		ItemTemplate riot = Shadowrun6Core.getItem(ItemTemplate.class, "riot_shield");
		assertNotNull(riot);

		CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(riot, null, CarryMode.CARRIED);
		assertNotNull(item);
		OperationResult<List<Modification>> modR = SR6GearTool.recalculate("", null, item);
		assertTrue(modR.wasSuccessful());
		
		List<CarriedItem<?>> alts = item.getAlternates();
		assertFalse("No alternates found",alts.isEmpty());
		CarriedItem alt = alts.get(0);
		
		assertNotNull(alt.getAsObject(SR6ItemAttribute.DAMAGE));
		assertEquals(4, ((Damage)alt.getAsObject(SR6ItemAttribute.DAMAGE).getModifiedValue()).getValue());
		
		ItemTemplate melee =  new ItemTemplate();
		melee.setId("unarmed");
		melee.setAttribute(SR6ItemAttribute.DAMAGE, new Damage(3, DamageType.STUN, null));
		CarriedItem<ItemTemplate> meleeItem = new CarriedItem<ItemTemplate>(melee, null, CarryMode.CARRIED);
		meleeItem.setUuid(ItemTemplate.UUID_UNARMED);
		meleeItem.setInjectedBy("UnitTest");
		SR6GearTool.recalculate("", null, meleeItem);
		Shadowrun6Character model = new Shadowrun6Character();
		model.addCarriedItem(item);
		model.addVirtualCarriedItem(meleeItem);
		CalculateMeleeAndUnarmed calc = new CalculateMeleeAndUnarmed(model);
		calc.process(new ArrayList<>());
	}

}
