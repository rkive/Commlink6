package de.rpgframework.shadowrun6.vehicle;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import de.rpgframework.genericrpg.PoolCalculation;
import de.rpgframework.genericrpg.ValueType;
import de.rpgframework.genericrpg.data.SkillSpecialization;
import de.rpgframework.genericrpg.items.CarriedItem;
import de.rpgframework.genericrpg.items.CarryMode;
import de.rpgframework.genericrpg.items.ItemAttributeNumericalValue;
import de.rpgframework.shadowrun.DamageElement;
import de.rpgframework.shadowrun.DamageType;
import de.rpgframework.shadowrun.ShadowrunAttribute;
import de.rpgframework.shadowrun6.SR6Skill;
import de.rpgframework.shadowrun6.SR6SkillValue;
import de.rpgframework.shadowrun6.Shadowrun6Character;
import de.rpgframework.shadowrun6.Shadowrun6Core;
import de.rpgframework.shadowrun6.Shadowrun6Tools;
import de.rpgframework.shadowrun6.items.AvailableSlot;
import de.rpgframework.shadowrun6.items.Damage;
import de.rpgframework.shadowrun6.items.ItemHook;
import de.rpgframework.shadowrun6.items.ItemSubType;
import de.rpgframework.shadowrun6.items.ItemTemplate;
import de.rpgframework.shadowrun6.items.ItemType;
import de.rpgframework.shadowrun6.items.ItemUtil;
import de.rpgframework.shadowrun6.items.SR6GearTool;
import de.rpgframework.shadowrun6.items.SR6ItemAttribute;
import de.rpgframework.shadowrun6.items.VehicleData;
import de.rpgframework.shadowrun6.items.VehicleData.VehicleType;

/**
 * @author prelle
 *
 */
public class VehicleTools {

	private final static Logger logger = System.getLogger("shadowrun6");
	
	//-------------------------------------------------------------------
	private static CarriedItem<ItemTemplate> getControlRig(Shadowrun6Character model) {
		CarriedItem<ItemTemplate> ctrlRig = model.getCarriedItem("control_rig");
		CarriedItem<ItemTemplate> actRig = model.getCarriedItem("active_control_rig");
		if (actRig==null) return ctrlRig;
		if (ctrlRig==null) return actRig;

		// Both present, return the one being actively used
		int rtg = ctrlRig.getAsValue(SR6ItemAttribute.RATING).getModifiedValue();
		int artg = actRig.getAsValue(SR6ItemAttribute.RATING).getModifiedValue();
		if (artg>=rtg)
			return actRig;
		return ctrlRig;		
	}
	
	//----------Calculate ram pools ------------------------------------------------
	public static VehicleUnarmedAttack getVehicleUnarmed(Shadowrun6Character model, CarriedItem<ItemTemplate> vehicle, VehicleOperationMode mode) {
		// Collect some necessary data
		CarriedItem<ItemTemplate> ctrlRig = getControlRig(model);
		int rigRating = (ctrlRig==null)?0:SR6GearTool.getRating( ctrlRig);
		CarriedItem<ItemTemplate> rcc = Shadowrun6Tools.getBestRCC(model);
		CarriedItem<ItemTemplate> simSenseOverdrive = model.getCarriedItem("simsense_overdrive");
		SR6Skill pilotSkill = Shadowrun6Core.getSkill("piloting");
		SkillSpecialization<SR6Skill> spec = getSpecializationForVehicle(vehicle.getResolved());
		String specS = (spec!=null)?spec.getId():null;

		List<PoolCalculation<Integer>> driverPilotPool = Shadowrun6Tools.getSkillPoolCalculationWithoutAttribute(model, pilotSkill, specS).getCalculation(ValueType.NATURAL);
		if (mode==VehicleOperationMode.AUTONOMOUS) {
			CarriedItem<ItemTemplate> autosoft = vehicle.getEmbeddedItem("maneuvering");
			driverPilotPool.clear();
			if (autosoft == null)
				driverPilotPool.add(new PoolCalculation<Integer>(-1, Shadowrun6Core.getItem(ItemTemplate.class,"maneuvering").getName()));
			else
				driverPilotPool.add(new PoolCalculation<Integer>(ItemUtil.getRating(autosoft), autosoft.getNameWithRating()));
		} else if (mode==VehicleOperationMode.RCC) {
			if (rcc==null) return null;
			CarriedItem<ItemTemplate> autosoftRcc = rcc.getEmbeddedItem("maneuvering");
			CarriedItem<ItemTemplate> autosoftDrone = vehicle.getEmbeddedItem("maneuvering");
			CarriedItem<ItemTemplate> autosoft = autosoftRcc;
			if (autosoftRcc==null || (autosoftDrone!=null && (ItemUtil.getRating(autosoftDrone) > ItemUtil.getRating(autosoftRcc))))
				autosoft = autosoftDrone;
			driverPilotPool.clear();
			if (autosoft == null)
				driverPilotPool.add(new PoolCalculation<Integer>(-1, Shadowrun6Core.getItem(ItemTemplate.class,"maneuvering").getName()));
			else
				driverPilotPool.add(new PoolCalculation<Integer>(ItemUtil.getRating(autosoft), autosoft.getNameWithRating()));
		}
		
		int sensor = vehicle.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue();
		int body   = vehicle.getAsValue(SR6ItemAttribute.BODY).getModifiedValue();
		int armor  = vehicle.getAsValue(SR6ItemAttribute.ARMOR).getModifiedValue();
		int pilot  = vehicle.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue();

		ToIntFunction<PoolCalculation<Integer>> pcInt = new ToIntFunction<PoolCalculation<Integer>>() {
			public int applyAsInt(PoolCalculation<Integer> pc) {
				return pc.value;
			}
		};
		VehicleUnarmedAttack ret = new VehicleUnarmedAttack(mode);
		// Damage is (Body / 2, rounded up, +1 for each current Speed Interval)
		// Physical damage
		int dmg= Math.round(body/2.0f);
		ret.setDamage(new Damage(dmg, DamageType.PHYSICAL, DamageElement.REGULAR));

		switch (mode) {
		case DRIVING:
			// The Attack Rating of a vehicle is Piloting of the driver + Sensor.
			List<PoolCalculation<Integer>> arPool = new ArrayList<>(driverPilotPool);
			arPool.add(new PoolCalculation<Integer>(sensor, SR6ItemAttribute.SENSORS.getName()));
			int ar = (int)arPool.stream().collect(Collectors.summarizingInt(pc -> pc.value)).getSum();
			ret.setAttackRating(ar);
			ret.setAttackRating(arPool);
			// logger.log(Level.INFO,"Unarmed: "+vehicle.getNameWithoutRating()+": AR   driven "+arPool);
			// The attempt to hit another being or vehicle is an Opposed test
			// — Piloting + Reaction vs. opponent
			List<PoolCalculation<Integer>> attPool = new ArrayList<>(driverPilotPool);
			attPool.addAll(model.getAttribute(ShadowrunAttribute.REACTION).getPool().getCalculation(ValueType.NATURAL));
			//logger.debug("Unarmed(MANUAL  ): "+vehicle.getName()+"::  "+attPool);
			int dice = (int)attPool.stream().collect(Collectors.summarizingInt(pc -> pc.value)).getSum();
			ret.setPool(dice);
			ret.setPool(attPool);
			break;
		case JUMPED_IN:
			// The Attack Rating of a vehicle is Piloting of the driver + Sensor.
			if (ctrlRig==null)
				return null;
			arPool = new ArrayList<>(driverPilotPool);
			arPool.add(new PoolCalculation(sensor, SR6ItemAttribute.SENSORS.getName()));
			// arPool.add(new PoolCalculation(rigRating, ctrlRig.getNameWithRating())); rating of rig is not added to AR
			ar = (int)arPool.stream().collect(Collectors.summarizingInt(pc -> pc.value)).getSum();
			ret.setAttackRating(ar);
			ret.setAttackRating(arPool);
			// logger.info("Unarmed: "+vehicle.getName()+": Rigged "+arPool);
			// The attempt to hit another being or vehicle is an Opposed test Piloting + Intuition vs. opponent if using normal rig, Piloting+REA if using active rig
			attPool = new ArrayList<>(driverPilotPool);
			if (ctrlRig==model.getCarriedItem("active_control_rig")) {
				attPool.addAll(model.getAttribute(ShadowrunAttribute.REACTION).getPool().getCalculation(ValueType.NATURAL));
			} else {
			attPool.addAll(model.getAttribute(ShadowrunAttribute.INTUITION).getPool().getCalculation(ValueType.NATURAL));
				if (simSenseOverdrive!=null) {
					int maxAdd = 4 - model.getAttribute(ShadowrunAttribute.INTUITION).getModifier();
					int add  = Math.min(maxAdd, simSenseOverdrive.getDistributed());
					attPool.add(new PoolCalculation(add, simSenseOverdrive.getNameWithRating()));
				}
			}
			attPool.add(new PoolCalculation(rigRating, ctrlRig.getNameWithRating()));
			//logger.info("Unarmed: "+vehicle.getName()+": Rigged "+attPool);
			dice = (int)attPool.stream().collect(Collectors.summarizingInt(pc -> pc.value)).getSum();
			ret.setPool(dice);
			ret.setPool(attPool);
			logger.log(Level.INFO,"Unarmed(JUMPED ): "+vehicle.getNameWithoutRating()+"::  "+attPool);
			break;
		case AUTONOMOUS:
			arPool = new ArrayList<>(driverPilotPool);
			arPool.add(new PoolCalculation(sensor, SR6ItemAttribute.SENSORS.getName()));
			ar = (int) arPool.stream().collect(Collectors.summarizingInt(pc -> pc.value)).getSum();
			ret.setAttackRating(ar);
			ret.setAttackRating(arPool);
			//			logger.info("Unarmed: "+vehicle.getName()+": Autonom "+arPool);
			// The attempt to hit another being or vehicle is an Opposed test
			// — Piloting + Reaction vs.
			attPool = new ArrayList<>(driverPilotPool);
			attPool.add(new PoolCalculation(pilot, SR6ItemAttribute.PILOT.getName()));
			dice = (int) attPool.stream().collect(Collectors.summarizingInt(pc -> pc.value)).getSum();
			ret.setPool(dice);
			ret.setPool(attPool);
			//logger.info("Unarmed(AUTONOM): "+vehicle.getName()+"::  "+attPool);
			break;
		case RCC:
			arPool = new ArrayList<>(driverPilotPool);
			arPool.add(new PoolCalculation(sensor, SR6ItemAttribute.SENSORS.getName()));
			ar = (int) arPool.stream().collect(Collectors.summarizingInt(pc -> pc.value)).getSum();
			ret.setAttackRating(ar);
			ret.setAttackRating(arPool);
			//			logger.info("Unarmed: "+vehicle.getName()+": RCC "+arPool);
			// The attempt to hit another being or vehicle is an Opposed test
			// — Piloting + Reaction vs.
			attPool = new ArrayList<>(driverPilotPool);
			attPool.add(new PoolCalculation(pilot, SR6ItemAttribute.PILOT.getName()));
			dice = (int) attPool.stream().collect(Collectors.summarizingInt(pc -> pc.value)).getSum();
			//logger.info("Unarmed(RCC    ): "+vehicle.getName()+"::  "+attPool);
			ret.setPool(dice);
			ret.setPool(attPool);
			break;
		}

		return ret;
	}

//----------Create list of ram attack pools -----------------------------------------------
	public static List<VehicleUnarmedAttack> getVehicleRamming(Shadowrun6Character model, CarriedItem vehicle) {
		List<VehicleUnarmedAttack> list = new ArrayList<>();
		list.add(getVehicleUnarmed(model, vehicle, VehicleOperationMode.DRIVING));
		list.add(getVehicleUnarmed(model, vehicle, VehicleOperationMode.AUTONOMOUS));
		VehicleUnarmedAttack rcc = getVehicleUnarmed(model, vehicle, VehicleOperationMode.RCC);
		if (rcc!=null)
			list.add(rcc);
		VehicleUnarmedAttack rigged = getVehicleUnarmed(model, vehicle, VehicleOperationMode.JUMPED_IN);
		if (rigged!=null)
			list.add(rigged);
		return list;
	}

//----------Get weapons --------------------------------------------------------
	public static List<CarriedItem<ItemTemplate>> getVehicleWeapons(Shadowrun6Character model, CarriedItem<ItemTemplate> vehicle) {
		List<CarriedItem<ItemTemplate>> list = new ArrayList<>();
		ItemType type = vehicle.getAsObject(SR6ItemAttribute.ITEMTYPE).getModifiedValue();
		// Ensure it is a drone or vehicle
		if (!List.of(ItemType.droneTypes()).contains(type) && type!=ItemType.VEHICLES)
			return list;

		vehicle.getEffectiveAccessories().forEach( ci -> {
			// Add all mounted weapons
			if (ItemType.isWeapon(ci)) {
				list.add(ci);
			}
			
/* code below has been replaced by code above. Restriction: factory installed weapons need to be mounted directly into hardpoint not via weapon_mount 
			// Medium
			AvailableSlot slot = ci.getSlot(ItemHook.VEHICLE_WEAPON);
			if (slot!=null && !slot.getAllEmbeddedItems().isEmpty())
				list.add(slot.getAllEmbeddedItems().get(0));
			// Large
			slot = ci.getSlot(ItemHook.VEHICLE_WEAPON_LARGE);
			if (slot!=null && !slot.getAllEmbeddedItems().isEmpty())
				list.add(slot.getAllEmbeddedItems().get(0));
			// Small
			slot = ci.getSlot(ItemHook.VEHICLE_WEAPON_SMALL);
			if (slot!=null && !slot.getAllEmbeddedItems().isEmpty())
				list.add(slot.getAllEmbeddedItems().get(0));
			// Hardpoints
			slot = ci.getSlot(ItemHook.VEHICLE_HARDPOINT);
			if (slot!=null && !slot.getAllEmbeddedItems().isEmpty())
				slot.getAllEmbeddedItems().stream()
					.filter(item -> ItemType.isWeapon(item))
					.map(item -> {System.err.println("VehicleTools.getVehicleWeapons: "+item);return item;})
					.forEach(item -> list.add(item));
*/		
		});

		return list;
	}

//--------------------------------------------------------------------
		/**
		 * Get the dice pools for using the weapon in a drone
		 * Rigged = Riggers skill with replaced physical attribute plus implanted 
		 *          ctrlrig rating
		 * RCC    = Autosoft + Sensor
		 */

		public static DronePool getDroneWeaponPool(Shadowrun6Character model, CarriedItem drone,  CarriedItem weapon) {
			return getDroneWeaponPool(model, drone, Shadowrun6Tools.getBestRCC(model), weapon);
		}

		public static DronePool getDroneWeaponPool(Shadowrun6Character model, CarriedItem<ItemTemplate> drone, CarriedItem<ItemTemplate> rcc, CarriedItem<ItemTemplate> weapon) {
			// Collect some necessary data
			// logger.log(Level.WARNING, "TODO: getDroneWeaponPool not implemented yet");
			CarriedItem<ItemTemplate> rig = getControlRig(model);
			int rigRating = (rig==null)?0:rig.getDecision(ItemTemplate.UUID_RATING).getValueAsInt();

			DronePool pool = new DronePool();
			/*
			 *  Rigged
			 */
			SR6Skill skill = Shadowrun6Core.getSkill("engineering");
			if (rig==null) {
				pool.rigged = 0;
			} else {
				pool.rigged = Shadowrun6Tools.getSkillPool(model, skill, "gunnery").getValue(ValueType.NATURAL);
				// add rig rating (German rules have changed to also include "alle Proben zum Betrieb des Fahrzeugs", thus no differentiation needed)
				int modifier = (model.getSkillValue(skill)!=null)?model.getSkillValue(skill).getModifier():0;
				int maxBonusRemain = (4-modifier);
				pool.rigged += Math.min(maxBonusRemain, rigRating);
			}

			/*
			 * Driven (using Gunnery)
			 */
			pool.manual = Shadowrun6Tools.getSkillPool(model, skill, "gunnery").getNatural();

			/*
			 * RCC
			 * Requires targeting autosoft on RCC
			 */
			CarriedItem<ItemTemplate> rccAutosoft = null;
			if (rcc==null) {
				pool.viaRCC = 0;
			} else {
				// Search matching autosoft
				// boolean anyTargeting = false;
				CarriedItem<ItemTemplate> autosoft = null;
				for (CarriedItem<ItemTemplate> item : rcc.getEffectiveAccessories()) {
					if (!item.getKey().equals("targeting"))
						continue;
				// anyTargeting = true;
					UUID weaponDec = UUID.fromString("2baf4c6e-417b-4d1a-943c-edfa816d50bf");
					if (item.getDecision(weaponDec)==null) {
						logger.log(Level.WARNING,"Weapon model of targeting autosoft not defined in drone "+drone);
					} else if (item.getDecision(weaponDec).getValue().equals(weapon.getKey()))
						autosoft = item;
				}
				if (autosoft!=null) {
					// Matching autosoft
					rccAutosoft = autosoft;
					pool.viaRCC = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue() + autosoft.getDecision(ItemTemplate.UUID_RATING).getValueAsInt();
				} else {
					// If the drone does not have the [Weapon] Targeting autosoft, then use Sensor rating – 1.
					pool.viaRCC = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue()-1;
				}
			}
			// Autonomous
			// Search matching autosoft
			// boolean anyTargeting = false;
			CarriedItem<ItemTemplate> autosoft = null;
			ItemType weaponType = weapon.getAsObject(SR6ItemAttribute.ITEMTYPE).getModifiedValue();
			switch (weaponType) {
			case WEAPON_CLOSE_COMBAT:
				for (CarriedItem<ItemTemplate> item : drone.getEffectiveAccessories()) {
					if (!item.getKey().equals("soft_close_combat"))
						continue;
				// anyTargeting = true;
					UUID weaponDec = UUID.fromString("2baf4c6e-417b-4d1a-943c-edfa816d50bf");
					if (item.getDecision(weaponDec)==null) {
						logger.log(Level.WARNING,"Weapon model of soft_close_combat autosoft not defined in drone " + drone);
					} else if (item.getDecision(weaponDec).getValue().equals(weapon.getKey()))
						autosoft = item;
				}
				break;
			default:
				for (CarriedItem<ItemTemplate> item : drone.getEffectiveAccessories()) {
					if (!item.getKey().equals("targeting"))
						continue;
				// anyTargeting = true;
					UUID weaponDec = UUID.fromString("2baf4c6e-417b-4d1a-943c-edfa816d50bf");
					if (item.getDecision(weaponDec)==null) {
						logger.log(Level.WARNING, "Weapon model of targeting autosoft not defined in drone " + drone);
					} else if (item.getDecision(weaponDec).getValue().equals(weapon.getKey()))
						autosoft = item;
				}
			}
			if (autosoft!=null) {
				// Matching autosoft
				pool.autonomous = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue() + autosoft.getDecision(ItemTemplate.UUID_RATING).getValueAsInt();
			} else {
				// If the drone does not have the [Weapon] Targeting autosoft, then use Sensor rating – 1.
				pool.autonomous = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue()-1;
			}
			// if the local autosoft is better, the rcc controlled drone may use this instead
			if (rccAutosoft!=null && autosoft!=null && autosoft.getDecision(ItemTemplate.UUID_RATING).getValueAsInt()>rccAutosoft.getDecision(ItemTemplate.UUID_RATING).getValueAsInt()) {
				pool.viaRCC = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue() + autosoft.getDecision(ItemTemplate.UUID_RATING).getValueAsInt();
			}
			if (rccAutosoft==null && autosoft!=null) {
				pool.viaRCC = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue() + autosoft.getDecision(ItemTemplate.UUID_RATING).getValueAsInt();
			}

			return pool;
		}

//------------- Get monitor size---------------------------------------------
		public static int[] getDroneMonitorArray(CarriedItem<ItemTemplate> item) {
			ItemAttributeNumericalValue val = item.getAsValue(SR6ItemAttribute.BODY);
			if (val==null)
				throw new IllegalArgumentException("Item does not have a BODY attribute");
			int add = 8 + Math.round(val.getModifiedValue()/2.0f);
			int[] ret = new int[add];

			int start = 0;
			int every = 3;

			for (int i=start; i<ret.length; i++) {
				ret[i] = - ((i+1-start)/every);
			}
			return ret;
		}

//---------- Get pools --------------------------------------------------------
		/**
		 * Determine the dice pool in different modes when using the drone.
		 * May be 0, if not possible
		 * @param model  Character
		 * @param drone  Drone/Vehicle
		 * @param action Action to perform
		 * @return Pool object or null, if not a drone
		 */
		public static DronePool getDronePool(Shadowrun6Character model, CarriedItem<ItemTemplate> drone, DroneAction action) {
			logger.log(Level.INFO, "{2}....{0}....{1}",drone.getNameWithoutRating(),action, model.getName());
			// Ensure it is a drone or vehicle
			if (!Shadowrun6Tools.isType(drone, ItemType.vehicleTypes())) {
				logger.log(Level.ERROR,"{0} is neither a vehicle, nor a drone", drone);
				return null;
			}

			// Collect some necessary data
			CarriedItem<ItemTemplate> ctrlRig = getControlRig(model);
			int rigRating = (ctrlRig==null)?0:SR6GearTool.getRating( ctrlRig);
			CarriedItem<ItemTemplate> rcc = Shadowrun6Tools.getBestRCC(model);
			CarriedItem<ItemTemplate> simSenseOverdrive = model.getCarriedItem("simsense_overdrive");
			// logger.log(Level.INFO, "Rating of control rig: {0}", rigRating);
			// logger.log(Level.INFO, "RCC                  : {0}", rcc);

			DronePool pool = new DronePool();
			SR6Skill pilotSkill = Shadowrun6Core.getSkill("piloting");
			SkillSpecialization<SR6Skill> spec = getSpecializationForVehicle(drone.getResolved());
			String specS = (spec!=null)?spec.getId():null;

			switch (action) {

			case EVADE:
				// Manually driven
				pool.manual = Shadowrun6Tools.getSkillPool(model, pilotSkill, ShadowrunAttribute.REACTION, specS).getValue(ValueType.NATURAL);

				// Jumped In (Assumption: Use Piloting skill to evade when jumped in, use INTUITION instead REACTION as jumped in attribute for normal rig, REA for active rig)
				if (ctrlRig!=null) {
					int modifier = (model.getSkillValue(pilotSkill)!=null)?model.getSkillValue(pilotSkill).getModifier():0;
					int maxRigBonus = Math.min( (4-modifier), rigRating);
					if (ctrlRig==model.getCarriedItem("active_control_rig")) {
						pool.rigged = Shadowrun6Tools.getSkillPool(model, pilotSkill, ShadowrunAttribute.REACTION, specS).getValue(ValueType.NATURAL) + maxRigBonus;
						} else {
						pool.rigged = Shadowrun6Tools.getSkillPool(model, pilotSkill, ShadowrunAttribute.INTUITION, specS).getValue(ValueType.NATURAL) + maxRigBonus;
						if (simSenseOverdrive!=null) {
							int maxAdd = 4 - model.getAttribute(ShadowrunAttribute.INTUITION).getModifier();
							pool.rigged += Math.min(maxAdd, SR6GearTool.getRating( simSenseOverdrive ));
						}
					}
				} else {
					// Jumped in impossible without rig
					pool.rigged = 0;
				}

				// Via RCC: Requires autosoft evasion on rcc or drone
				if (rcc==null) {
					pool.viaRCC = 0;
					} else {
					CarriedItem<ItemTemplate> autosoftRcc = rcc.getEmbeddedItem("evasion");
					CarriedItem<ItemTemplate> autosoftDrone = drone.getEmbeddedItem("evasion");
					if (autosoftRcc==null && autosoftDrone==null) 
						pool.viaRCC = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue()-1; { 
						if (autosoftDrone!=null) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue() + SR6GearTool.getRating(autosoftDrone);
						if (autosoftRcc!=null) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue() + SR6GearTool.getRating(autosoftRcc);
						if (autosoftRcc!=null && autosoftDrone!=null && SR6GearTool.getRating( autosoftDrone )>SR6GearTool.getRating( autosoftRcc )) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue() + SR6GearTool.getRating( autosoftDrone );						
						}
					}
				
				// Autonomous
				CarriedItem<ItemTemplate> autosoft = drone.getEmbeddedItem("evasion");
				if (autosoft==null)
					pool.autonomous = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue()-1;
				else
					pool.autonomous = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue() + SR6GearTool.getRating(autosoft);
				break;

			case PERCEPTION:
				// Manually driven
				pool.manual = Shadowrun6Tools.getSkillPool(model, Shadowrun6Core.getSkill("perception"), ShadowrunAttribute.INTUITION).getValue(ValueType.NATURAL);

				// Jumped in (dont make an assumption about specializations, use INTUITION as jumped in attribute, same as usual)
				if (ctrlRig!=null) {
					pool.rigged = Shadowrun6Tools.getSkillPool(model, Shadowrun6Core.getSkill("perception"), ShadowrunAttribute.INTUITION).getValue(ValueType.NATURAL);
					int modifier = (model.getSkillValue(Shadowrun6Core.getSkill("perception"))!=null)?model.getSkillValue(Shadowrun6Core.getSkill("perception")).getModifier():0;
					int cappedRigBonus = Math.min( (4-modifier), rigRating);
					pool.rigged += cappedRigBonus;
					if (simSenseOverdrive!=null) {
						int maxAdd = 4 - model.getAttribute(ShadowrunAttribute.INTUITION).getModifier();
						pool.rigged += Math.min(maxAdd, SR6GearTool.getRating( simSenseOverdrive ));
					}
				} else {
					// Jumped in impossible without rig
					pool.rigged = 0;
				}

				// Via RCC: Requires autosoft clearsight on rcc or drone
				if (rcc==null) {
					pool.viaRCC = 0;
					} else {
					CarriedItem<ItemTemplate> autosoftRcc = rcc.getEmbeddedItem("clearsight");
					CarriedItem<ItemTemplate> autosoftDrone = drone.getEmbeddedItem("clearsight");
					if (autosoftRcc==null && autosoftDrone==null) 
						pool.viaRCC = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue()-1; { 
						if (autosoftDrone!=null) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue() + SR6GearTool.getRating(autosoftDrone);
						if (autosoftRcc!=null) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue() + SR6GearTool.getRating(autosoftRcc);
						if (autosoftRcc!=null && autosoftDrone!=null && SR6GearTool.getRating( autosoftDrone )>SR6GearTool.getRating( autosoftRcc )) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue() + SR6GearTool.getRating( autosoftDrone );						
						}
					}
				
				// Autonomous
				autosoft = drone.getEmbeddedItem("clearsight");
				if (autosoft==null)
					pool.autonomous = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue()-1;
				else
					pool.autonomous = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue() + SR6GearTool.getRating(autosoft);

				break;

			case CRACKING:
				// Manually driven
				pool.manual = Shadowrun6Tools.getSkillPool(model, Shadowrun6Core.getSkill("cracking"), ShadowrunAttribute.LOGIC, "electronic_warfare").getValue(ValueType.NATURAL);
				
				// Jumped in
				if (ctrlRig!=null) {
					pool.rigged = Shadowrun6Tools.getSkillPool(model, Shadowrun6Core.getSkill("cracking"), ShadowrunAttribute.LOGIC, "electronic_warfare").getValue(ValueType.NATURAL) + rigRating;
				} else {
					// Jumped in impossible without rig
					pool.rigged = 0;
				}

				// Via RCC: Requires autosoft electronic_warfare on rcc or drone
				if (rcc==null) {
					pool.viaRCC = 0;
					} else {
					CarriedItem<ItemTemplate> autosoftRcc = rcc.getEmbeddedItem("electronic_warfare");
					CarriedItem<ItemTemplate> autosoftDrone = drone.getEmbeddedItem("electronic_warfare");
					if (autosoftRcc==null && autosoftDrone==null) 
						pool.viaRCC = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue()-1; { 
						if (autosoftDrone!=null) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue() + SR6GearTool.getRating(autosoftDrone);
						if (autosoftRcc!=null) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue() + SR6GearTool.getRating(autosoftRcc);
						if (autosoftRcc!=null && autosoftDrone!=null && SR6GearTool.getRating( autosoftDrone )>SR6GearTool.getRating( autosoftRcc )) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue() + SR6GearTool.getRating( autosoftDrone );						
						}
					}
				
				// Autonomous
				autosoft = drone.getEmbeddedItem("electronic_warfare");
				if (autosoft==null)
					pool.autonomous = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue()-1;
				else
					pool.autonomous = drone.getAsValue(SR6ItemAttribute.SENSORS).getModifiedValue() + SR6GearTool.getRating(autosoft);
				break;

			case STEALTH:
				// Manually driven
				pool.manual = Shadowrun6Tools.getSkillPool(model, Shadowrun6Core.getSkill("stealth"), ShadowrunAttribute.AGILITY).getValue(ValueType.NATURAL);

				// Jumped in (assume rig gives bonus to stealth in US "all tests involving operation of a vehicle" but not in German "bei Proben, bei denen es um das Steuern eines Fahrzeuges geht" UPDATE: German rules have changed to also include "alle Proben zum Betrieb des Fahrzeugs", thus no differentiation needed)
				if (ctrlRig!=null) {
					if (ctrlRig==model.getCarriedItem("active_control_rig")) {
						pool.rigged = Shadowrun6Tools.getSkillPool(model, Shadowrun6Core.getSkill("stealth"), ShadowrunAttribute.AGILITY).getValue(ValueType.NATURAL);
						} else {
						pool.rigged = Shadowrun6Tools.getSkillPool(model, Shadowrun6Core.getSkill("stealth"), ShadowrunAttribute.LOGIC).getValue(ValueType.NATURAL);
						}
					SR6SkillValue stealth = model.getSkillValue(Shadowrun6Core.getSkill("stealth"));
					int cappedRigBonus = (stealth==null)?rigRating:Math.min( (4-stealth.getModifier()), rigRating);
					pool.rigged += cappedRigBonus;
				} else {
					// Jumped in impossible without rig
					pool.rigged = 0;
				}

				// Via RCC: Requires autosoft stealth_auto on rcc or drone
				if (rcc==null) {
					pool.viaRCC = 0;
					} else {
					CarriedItem<ItemTemplate> autosoftRcc = rcc.getEmbeddedItem("stealth_auto");
					CarriedItem<ItemTemplate> autosoftDrone = drone.getEmbeddedItem("stealth_auto");
					if (autosoftRcc==null && autosoftDrone==null) 
						pool.viaRCC = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue()-1; { 
						if (autosoftDrone!=null) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue() + SR6GearTool.getRating(autosoftDrone);
						if (autosoftRcc!=null) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue() + SR6GearTool.getRating(autosoftRcc);
						if (autosoftRcc!=null && autosoftDrone!=null && SR6GearTool.getRating( autosoftDrone )>SR6GearTool.getRating( autosoftRcc )) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue() + SR6GearTool.getRating( autosoftDrone );						
						}
					}

				// Autonomous
				autosoft = drone.getEmbeddedItem("stealth_auto");
				if (autosoft==null)
					pool.autonomous = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue()-1;
				else
					pool.autonomous = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue() + SR6GearTool.getRating(autosoft);
				break;

			case PILOT:
				// Manually driven
				pool.manual = Shadowrun6Tools.getSkillPool(model, pilotSkill, ShadowrunAttribute.REACTION, specS).getValue(ValueType.NATURAL);

				// Jumped in (use INTUITION instead REACTION (jumped in attribute) for normal rig, keep REA for active rig)
				if (ctrlRig!=null) {
					int modifier = (model.getSkillValue(pilotSkill)!=null)?model.getSkillValue(pilotSkill).getModifier():0;
					int maxRigBonus = Math.min( (4-modifier), rigRating);
					if (ctrlRig==model.getCarriedItem("active_control_rig")) {
						pool.rigged = Shadowrun6Tools.getSkillPool(model, pilotSkill, ShadowrunAttribute.REACTION, specS).getValue(ValueType.NATURAL) + maxRigBonus;
						} else {
						pool.rigged = Shadowrun6Tools.getSkillPool(model, pilotSkill, ShadowrunAttribute.INTUITION, specS).getValue(ValueType.NATURAL) + maxRigBonus;
						if (simSenseOverdrive!=null) {
							int maxAdd = 4 - model.getAttribute(ShadowrunAttribute.INTUITION).getModifier();
							pool.rigged += Math.min(maxAdd, SR6GearTool.getRating( simSenseOverdrive ));
							}
						}
				} else {
					// Jumped in impossible without rig
					pool.rigged = 0;
				}

				// Via RCC: Requires autosoft maneuvering on rcc or drone
				if (rcc==null) {
					pool.viaRCC = 0;
					} else {
					CarriedItem<ItemTemplate> autosoftRcc = rcc.getEmbeddedItem("maneuvering");
					CarriedItem<ItemTemplate> autosoftDrone = drone.getEmbeddedItem("maneuvering");
					if (autosoftRcc==null && autosoftDrone==null) 
						pool.viaRCC = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue()-1; { 
						if (autosoftDrone!=null) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue() + SR6GearTool.getRating(autosoftDrone);
						if (autosoftRcc!=null) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue() + SR6GearTool.getRating(autosoftRcc);
						if (autosoftRcc!=null && autosoftDrone!=null && SR6GearTool.getRating( autosoftDrone )>SR6GearTool.getRating( autosoftRcc )) 
							pool.viaRCC = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue() + SR6GearTool.getRating( autosoftDrone );						
						}
					}
				
				// Autonomous
				autosoft = drone.getEmbeddedItem("maneuvering");
				if (autosoft==null)
					pool.autonomous = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue()-1;
				else
					pool.autonomous = drone.getAsValue(SR6ItemAttribute.PILOT).getModifiedValue() + SR6GearTool.getRating(autosoft);
				break;

			case DEFENSE_RATING:
				// "the Defense Rating for vehicles is Piloting of the driver + Armor"
				int armor = drone.getAsValue(SR6ItemAttribute.ARMOR).getModifiedValue();

				// Driven
				pool.manual = Shadowrun6Tools.getSkillPoolCalculationWithoutAttribute(model, pilotSkill, specS).getValue(ValueType.NATURAL) + armor;

				// Jumped in
				if (ctrlRig!=null) {
					pool.rigged = Shadowrun6Tools.getSkillPoolCalculationWithoutAttribute(model, pilotSkill, specS).getValue(ValueType.NATURAL) + armor;
					//				Rig rating does not add to DR
					//					int modifier = (model.getSkillValue(pilotSkill)!=null)?model.getSkillValue(pilotSkill).getModifier():0;
					//					int cappedRigBonus = Math.min( (4-modifier), rigRating);
					//					pool.rigged += cappedRigBonus;
				} else {
					// Jumped in impossible without rig
					pool.rigged = 0;
				}

				// Via RCC: Requires autosoft maneuvering on rcc or drone
				if (rcc==null) {
					pool.viaRCC = 0;
					} else {
					CarriedItem<ItemTemplate> autosoftRcc = rcc.getEmbeddedItem("maneuvering");
					CarriedItem<ItemTemplate> autosoftDrone = drone.getEmbeddedItem("maneuvering");
					if (autosoftRcc==null && autosoftDrone==null) 
						pool.viaRCC = armor; else { 
						if (autosoftDrone!=null) 
							pool.viaRCC = armor + SR6GearTool.getRating(autosoftDrone);
						if (autosoftRcc!=null) 
							pool.viaRCC = armor + SR6GearTool.getRating(autosoftRcc);
						if (autosoftRcc!=null && autosoftDrone!=null && SR6GearTool.getRating( autosoftDrone )>SR6GearTool.getRating( autosoftRcc )) 
							pool.viaRCC = armor + SR6GearTool.getRating(autosoftDrone);						
						}
					}

				// Autonomous
				autosoft = drone.getEmbeddedItem("maneuvering");
				if (autosoft==null)
					pool.autonomous = armor;
				else
					pool.autonomous = armor + SR6GearTool.getRating(autosoft);
				break;
			default:
				logger.log(Level.WARNING, "DroneAction "+action+" not implemented yet");
			}

			return pool;
		}

//------------- Create accessory string --------------------------------------
		public static String getVehicleAccessoryString(CarriedItem<ItemTemplate> item) {
			class Counted {
				CarriedItem<ItemTemplate> inst;
				int count;
				public Counted(CarriedItem<ItemTemplate> item) {
					inst = item;
					count=1;
				}
				public String toString() {
					if (count==1) return inst.getNameWithRating();
					return inst.getNameWithRating()+" ("+count+"x)";
				}
			}
			Map<ItemTemplate, Counted> map = new LinkedHashMap<>();
			List<String> list = new ArrayList<>();
			for (CarriedItem<ItemTemplate> ci : item.getEffectiveAccessories()) {
				ItemSubType sub = (ItemSubType)ci.getAsObject(SR6ItemAttribute.ITEMSUBTYPE).getModifiedValue();
			//				if (ci!=null && ci.getUsedAsSubType()!=null)
			//					sub = ci.getUsedAsSubType();
			//				else {
			//					if (ci.getUsedAsType()!=null && ci.getItem().getSubtype(ci.getUsedAsType())!=null) {
			//						sub = ci.getItem().getSubtype(ci.getUsedAsType());
			//					} else
			//						logger.warn("No subtype set for "+ci+" / "+ci.getUsedAsType()+" / "+ci.getItem().getSubtype(ci.getUsedAsType()));
			//				}
				switch (sub) {
				case HACKING_PROGRAM:
				case BASIC_PROGRAM:
				case RIGGER_PROGRAM:
				case AUTOSOFT:
				case SKILLSOFT:
					break;
				default:
					// Don't print hardpoints (Pop-Out Concealment is a mount mod, not a hardpoint)
					if (ci.getResolved().getId().startsWith("hardpoint") && !ci.getResolved().getId().equals("hardpoint_concealment"))
						continue;
					if (ci.getResolved().getId().startsWith("modslot_"))
						continue;
					if (ci.getResolved().getId().startsWith("improved_"))
						continue;
					if (ci.getResolved().getId().startsWith("enhanced_"))
						continue;
					if (ci.getResolved().getId().startsWith("weapon_mount"))
						continue;
					// Sum up
					if (map.containsKey(ci.getResolved())) {
						map.get(ci.getResolved()).count++;
					} else {
						map.put(ci.getResolved(), new Counted(ci));
					}
				}
			};
			map.values().forEach(c-> list.add(c.toString()));

			String mods = String.join(", ", list);
			return mods;
		}

//------------- Get correct piloting specialization --------------------------------
		public static SkillSpecialization getSpecializationForVehicle(ItemTemplate item) {
			SR6Skill pilot = Shadowrun6Core.getSkill("piloting");
			if (pilot==null)
				return null;

			ItemType typeI = item.getItemType(CarryMode.CARRIED);
			ItemSubType typeS = item.getItemSubtype(CarryMode.CARRIED);
			switch (typeI) {
			case VEHICLES:
				switch (typeS) {
				case BIKES:
				case ATVS:
				case CARS:
				case TRUCKS:
				case VANS:
				case BUS:
				case TRACKED:
				case SPECIAL_VEHICLES:
				case WALKER:
					return pilot.getSpecialization("ground_craft") ;
				case HOVERCRAFT:
				case PWC:
				case BOATS:
				case SHIPS:
				case SUBMARINES:
					return pilot.getSpecialization("watercraft") ;
				case FIXED_WING:
				case ROTORCRAFT:
				case LAV:
				case LTAV:
				case GRAV:
				case SPACECRAFT:
				case VTOL:
					return pilot.getSpecialization("aircraft") ;
				default:
				}
				break;
			case DRONE_MICRO:
			case DRONE_MINI:
			case DRONE_SMALL:
			case DRONE_MEDIUM:
			case DRONE_LARGE:
				switch (typeS) {
				case GROUND:
				case ANTHRO:
					return pilot.getSpecialization("ground_craft") ;
				case AIR:
					return pilot.getSpecialization("aircraft") ;
				case AQUATIC:
					return pilot.getSpecialization("watercraft") ;
				}

			default:
			}
			return null;
		}

}
