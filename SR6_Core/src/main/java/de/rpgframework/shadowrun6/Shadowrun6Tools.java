package de.rpgframework.shadowrun6;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.rpgframework.MultiLanguageResourceBundle;
import de.rpgframework.ResourceI18N;
import de.rpgframework.character.ProcessingStep;
import de.rpgframework.core.BabylonEventBus;
import de.rpgframework.core.BabylonEventType;
import de.rpgframework.genericrpg.Pool;
import de.rpgframework.genericrpg.PoolCalculation;
import de.rpgframework.genericrpg.Possible;
import de.rpgframework.genericrpg.Reward;
import de.rpgframework.genericrpg.SetItem;
import de.rpgframework.genericrpg.SetItemValue;
import de.rpgframework.genericrpg.ValueType;
import de.rpgframework.genericrpg.chargen.ComplexDataItemController.RemoveMode;
import de.rpgframework.genericrpg.data.ApplyTo;
import de.rpgframework.genericrpg.data.AttributeValue;
import de.rpgframework.genericrpg.data.Choice;
import de.rpgframework.genericrpg.data.ComplexDataItem;
import de.rpgframework.genericrpg.data.ComplexDataItemValue;
import de.rpgframework.genericrpg.data.DataErrorException;
import de.rpgframework.genericrpg.data.DataItem;
import de.rpgframework.genericrpg.data.DataItemValue;
import de.rpgframework.genericrpg.data.Decision;
import de.rpgframework.genericrpg.data.GenericRPGTools;
import de.rpgframework.genericrpg.data.SkillSpecialization;
import de.rpgframework.genericrpg.data.SkillSpecializationValue;
import de.rpgframework.genericrpg.items.AItemEnhancement;
import de.rpgframework.genericrpg.items.CarriedItem;
import de.rpgframework.genericrpg.items.CarryMode;
import de.rpgframework.genericrpg.items.GearTool;
import de.rpgframework.genericrpg.items.ItemAttributeDefinition;
import de.rpgframework.genericrpg.items.ItemAttributeNumericalValue;
import de.rpgframework.genericrpg.items.ItemAttributeObjectValue;
import de.rpgframework.genericrpg.items.ItemAttributeValue;
import de.rpgframework.genericrpg.items.ItemEnhancementValue;
import de.rpgframework.genericrpg.items.Usage;
import de.rpgframework.genericrpg.items.formula.FormulaImpl;
import de.rpgframework.genericrpg.items.formula.FormulaTool;
import de.rpgframework.genericrpg.items.formula.VariableResolver;
import de.rpgframework.genericrpg.modification.AllowModification;
import de.rpgframework.genericrpg.modification.CheckModification;
import de.rpgframework.genericrpg.modification.DataItemModification;
import de.rpgframework.genericrpg.modification.Modification;
import de.rpgframework.genericrpg.modification.ModificationChoice;
import de.rpgframework.genericrpg.modification.Modifyable;
import de.rpgframework.genericrpg.modification.RelevanceModification;
import de.rpgframework.genericrpg.modification.ValueModification;
import de.rpgframework.genericrpg.requirements.AnyRequirement;
import de.rpgframework.genericrpg.requirements.ExistenceRequirement;
import de.rpgframework.genericrpg.requirements.Requirement;
import de.rpgframework.genericrpg.requirements.ValueRequirement;
import de.rpgframework.shadowrun.AdeptPower;
import de.rpgframework.shadowrun.AdeptPowerValue;
import de.rpgframework.shadowrun.ComplexForm;
import de.rpgframework.shadowrun.ComplexFormValue;
import de.rpgframework.shadowrun.CritterPower;
import de.rpgframework.shadowrun.CritterPowerValue;
import de.rpgframework.shadowrun.Focus;
import de.rpgframework.shadowrun.FocusValue;
import de.rpgframework.shadowrun.LifestyleQuality;
import de.rpgframework.shadowrun.MagicOrResonanceType;
import de.rpgframework.shadowrun.MetamagicOrEcho;
import de.rpgframework.shadowrun.MetamagicOrEchoValue;
import de.rpgframework.shadowrun.Quality;
import de.rpgframework.shadowrun.QualityValue;
import de.rpgframework.shadowrun.Ritual;
import de.rpgframework.shadowrun.RitualValue;
import de.rpgframework.shadowrun.SIN.FakeRating;
import de.rpgframework.shadowrun.ShadowrunAttribute;
import de.rpgframework.shadowrun.ShadowrunCharacter;
import de.rpgframework.shadowrun.SkillType;
import de.rpgframework.shadowrun.SpellValue;
import de.rpgframework.shadowrun.items.AmmunitionSlot;
import de.rpgframework.shadowrun.items.FireMode;
import de.rpgframework.shadowrun.proc.GetModificationsFromCritterPowers;
import de.rpgframework.shadowrun.proc.GetModificationsFromFoci;
import de.rpgframework.shadowrun.proc.GetModificationsFromMetaEchoes;
import de.rpgframework.shadowrun.proc.GetModificationsFromMetaType;
import de.rpgframework.shadowrun.proc.GetModificationsFromQualities;
import de.rpgframework.shadowrun.proc.RealSINUpdater;
import de.rpgframework.shadowrun6.items.AmmunitionType;
import de.rpgframework.shadowrun6.items.Damage;
import de.rpgframework.shadowrun6.items.ItemHook;
import de.rpgframework.shadowrun6.items.ItemSubType;
import de.rpgframework.shadowrun6.items.ItemTemplate;
import de.rpgframework.shadowrun6.items.ItemType;
import de.rpgframework.shadowrun6.items.ItemUtil;
import de.rpgframework.shadowrun6.items.SR6GearTool;
import de.rpgframework.shadowrun6.items.SR6ItemAttribute;
import de.rpgframework.shadowrun6.items.SR6ItemEnhancement;
import de.rpgframework.shadowrun6.items.SR6ItemFlag;
import de.rpgframework.shadowrun6.items.SR6ResolveTemplatesStep;
import de.rpgframework.shadowrun6.items.WeaponSize;
import de.rpgframework.shadowrun6.log.Logging;
import de.rpgframework.shadowrun6.modifications.ShadowrunCheckInfluence;
import de.rpgframework.shadowrun6.modifications.ShadowrunReference;
import de.rpgframework.shadowrun6.persist.WeaponDamageConverter;
import de.rpgframework.shadowrun6.proc.ApplyModificationsGeneric;
import de.rpgframework.shadowrun6.proc.BuildForbiddenSources;
import de.rpgframework.shadowrun6.proc.CalculateAttributePools;
import de.rpgframework.shadowrun6.proc.CalculateDerivedAttributes;
import de.rpgframework.shadowrun6.proc.CalculateEssence;
import de.rpgframework.shadowrun6.proc.CalculateExowareBonus;
import de.rpgframework.shadowrun6.proc.CalculateMeleeAndUnarmed;
import de.rpgframework.shadowrun6.proc.CalculatePersona;
import de.rpgframework.shadowrun6.proc.CalculateSkillPools;
import de.rpgframework.shadowrun6.proc.CleanVirtualItems;
import de.rpgframework.shadowrun6.proc.EnsureAttributePresence;
import de.rpgframework.shadowrun6.proc.FixCharacterStep;
import de.rpgframework.shadowrun6.proc.FixEssenceChanges;
import de.rpgframework.shadowrun6.proc.GetGearDefinitions;
import de.rpgframework.shadowrun6.proc.GetModificationsForDrakes;
import de.rpgframework.shadowrun6.proc.GetModificationsForShifters;
import de.rpgframework.shadowrun6.proc.GetModificationsFromCollectives;
import de.rpgframework.shadowrun6.proc.GetModificationsFromGear;
import de.rpgframework.shadowrun6.proc.GetModificationsFromMagicOrResonance;
import de.rpgframework.shadowrun6.proc.GetModificationsFromMentorSpirits;
import de.rpgframework.shadowrun6.proc.GetModificationsFromPowers;
import de.rpgframework.shadowrun6.proc.GetModificationsFromQualityPaths;
import de.rpgframework.shadowrun6.proc.GetModificationsFromTechniques;
import de.rpgframework.shadowrun6.proc.ResetModifications;
import de.rpgframework.shadowrun6.proc.SpecialRuleStep;

/**
 * @author prelle
 *
 */
public class Shadowrun6Tools {

	private final static Logger logger = System.getLogger("de.rpgframework.shadowrun6");

	private static MultiLanguageResourceBundle RES;

	private static class Counted {
		CarriedItem<ItemTemplate> inst;
		int count;
		public Counted(CarriedItem<ItemTemplate> item) {
			inst = item;
			count=1;
		}
		public String toString() {
			if (count==1) return inst.getNameWithoutRating();
			return inst.getNameWithoutRating()+" ("+count+"x)";
		}
	}

	
	public final static List<Class<? extends ProcessingStep>> RECALCULATE_STEPS = Arrays.asList(
		ResetModifications.class,
		CleanVirtualItems.class,
		FixCharacterStep.class,
		EnsureAttributePresence.class,
		GetModificationsFromMetaType.class,
		GetGearDefinitions.class,
		GetModificationsFromQualityPaths.class,
		BuildForbiddenSources.class,
		ApplyModificationsGeneric.class,
		GetModificationsForDrakes.class,
		GetModificationsForShifters.class,
		GetModificationsFromCollectives.class,
		ApplyModificationsGeneric.class,
		GetModificationsFromMagicOrResonance.class,
		GetModificationsFromQualities.class,
		GetModificationsFromMentorSpirits.class,
		ApplyModificationsGeneric.class,
		GetModificationsFromMetaEchoes.class,
		GetModificationsFromGear.class,
//		new GetModificationsFromMetamagicOrEchoes(),
		GetModificationsFromFoci.class,
		GetModificationsFromPowers.class,
		ApplyModificationsGeneric.class,
//		new ApplyAdeptPowerModifications(),
		GetModificationsFromTechniques.class,
		GetModificationsFromCritterPowers.class,
		ApplyModificationsGeneric.class,
		SpecialRuleStep.class,
		FixEssenceChanges.class,
		CalculateEssence.class,
		CalculatePersona.class,
		CalculateDerivedAttributes.class,
		CalculateAttributePools.class,
		CalculateSkillPools.class,
		CalculateMeleeAndUnarmed.class,
		CalculateExowareBonus.class,
		RealSINUpdater.class
	);

	public static class WeaponValues {
		private String name;
		private String dmg;
		private int[] attackRating;
		private int pool;
		private boolean inactive;
		public String getName() { return name; }
		public String getDamage() { return dmg; }
		public int[] getAttackRating() { return attackRating; }
		public int getPool() { return pool; }
		public boolean isInactive() { return inactive; }
		public String toString() {
			return name+"  DMG:"+dmg+"  AR:"+Arrays.toString(attackRating)+"  Pool:"+pool;
		}
		public WeaponValues() {}
		public WeaponValues(String n, int[] ar) {
			this.name = n;
			this.attackRating = ar;
		}
	}

	//-------------------------------------------------------------------
	static {
		RES = new MultiLanguageResourceBundle(Shadowrun6Tools.class.getName(), Locale.ENGLISH, Locale.GERMAN);
	}

	//-------------------------------------------------------------------
	public static List<ProcessingStep> getCharacterProcessingSteps(Shadowrun6Character model, Locale loc) {
		List<ProcessingStep> steps = new ArrayList<>();
		for (Class<? extends ProcessingStep> cls : RECALCULATE_STEPS) {
			Constructor<? extends ProcessingStep> cons =  null;
			try {
				cons = cls.getConstructor(Shadowrun6Character.class, Locale.class);
			} catch (Exception e) {
			}
			if (cons==null) {
				try { cons = cls.getConstructor(ShadowrunCharacter.class, Locale.class);	} catch (Exception e) { }
			}
			if (cons==null) {
				try { cons = cls.getConstructor(Shadowrun6Character.class);	} catch (Exception e) { }
			}
			if (cons==null) {
				try { cons = cls.getConstructor(ShadowrunCharacter.class);	} catch (Exception e) { }
			}

			if (cons!=null) {
				try {
					if (cons.getParameterCount()==2)
						steps.add( cons.newInstance(model, loc));
					else
						steps.add( cons.newInstance(model));
				} catch (Exception e) {
					logger.log(Level.ERROR, "Error instantiating "+cls);
					e.printStackTrace();
					System.exit(0);
				}
			} else {
				logger.log(Level.ERROR, "No constructor for "+cls);
				System.exit(1);
			}
		}

		return steps;
	}
	//-------------------------------------------------------------------
	public static void runProcessors(Shadowrun6Character model, Locale loc) {
		List<ProcessingStep> processChain = getCharacterProcessingSteps(model, loc);
		try {
			logger.log(Level.WARNING, "\n\nSTART: runProcessors: "+processChain.size()+"-------------------------------------------------------");
			List<Modification> unprocessed = new ArrayList<>();
			for (ProcessingStep processor : processChain) {
				unprocessed = processor.process(unprocessed);
				logger.log(Level.INFO, "------ after {0}     {1}|{2}",processor.getClass().getSimpleName(),model.getKarmaFree(), unprocessed);
			}
			logger.log(Level.DEBUG, "Remaining mods  = "+unprocessed);
			logger.log(Level.DEBUG, "STOP : runProcessors: "+processChain.size()+"-------------------------------------------------------");
		} catch (Exception e) {
			logger.log(Level.ERROR, "Failed calculating character "+model.getName(),e);
			BabylonEventBus.fireEvent(BabylonEventType.UI_MESSAGE, 2, "Failed calculating character "+model.getName(), e);
		}
	}

	//-------------------------------------------------------------------
	public static String getModificationSourceString(Object source) {
		if (source==null)
			return "Unknown source";
		if (source instanceof ShadowrunAttribute)
			return ((ShadowrunAttribute)source).getName();
		if (source instanceof SR6ItemAttribute)
			return ((SR6ItemAttribute)source).getName();
		if (source instanceof SR6Skill)
			return ((SR6Skill)source).getName();
//		if (source instanceof Technique)
//			return ((Technique)source).getName();
		if (source instanceof CarriedItem) {
			return ((CarriedItem)source).getNameWithRating();
		} else if (source instanceof AdeptPower) {
			return RES.getString("label.adeptpower")+" "+((AdeptPower)source).getName();
		} else if (source instanceof ItemTemplate) {
			return ((ItemTemplate)source).getName();
		} else if (source instanceof String) {
			return (String)source;
//		} else if (source instanceof FocusValue) {
//			return ((FocusValue)source).getName();
		} else if (source instanceof DataItem) {
			return ((DataItem)source).getName();
		} else if (source instanceof DataItemValue) {
			DataItem di = ((DataItemValue)source).getResolved();
			return di.getName();
		}
		logger.log(Level.WARNING,"Missing treatment for modification source: "+source.getClass());
		return "?"+source.getClass().getSimpleName();
	}

	//-------------------------------------------------------------------
	public static Function<Modification, String> modificationResolver(Locale loc) {
		return (m) -> getModificationString(m, loc);
	}

	//-------------------------------------------------------------------
	public static String getModificationString(Modification mod, Locale loc) {
		String ret = getModificationStringWithoutCond(mod, loc);
		if (ret==null) return null;
		if (mod.isConditional()) {
			return ret+" ("+RES.getString("modification.conditional", loc)+")";
		}
		return ret;
	}

	//-------------------------------------------------------------------
	private static String getValueString(ValueModification valMod, Locale loc) {
		String level = "";
		if ("$LEVEL".equals(valMod.getRawValue())) {
			level = RES.getString("modification.value.level",loc);
		} else if ("$LEVEL-1".equals(valMod.getRawValue())) {
				level = RES.getString("modification.value.level",loc)+"-1";
		} else if ("$LEVEL*2".equals(valMod.getRawValue())) {
			level = RES.getString("modification.value.level",loc)+"*2";
		} else if ("&INITIATION_RANK".equals(valMod.getRawValue())) {
			level = RES.getString("modification.value.initiation_rank",loc);
		} else if ("&SUBMERSION_RANK".equals(valMod.getRawValue())) {
			level = RES.getString("modification.value.submersion_rank",loc);
		} else if (valMod.getRawValue().startsWith("&")) {
			// Reference to attribute
			String attrKey = valMod.getRawValue().substring(1);
			ShadowrunAttribute attrib = ShadowrunAttribute.valueOf(attrKey);
			return attrib.getName(loc);
		} else if (valMod.getRawValue().contains(".")) {
			level = String.valueOf(valMod.getValueAsDouble());
		} else if (valMod.getReferenceType()==ShadowrunReference.ITEM_ATTRIBUTE && valMod.getKey().equals("SOFTWARE_TYPES")) {
			return valMod.getRawValue();
		} else
			level = String.valueOf(valMod.getValue());
		return level;
	}

	//-------------------------------------------------------------------
	private static String getValueString(ValueRequirement valMod, Locale loc) {
		String level = "";
		if ("$LEVEL".equals(valMod.getRawValue())) {
			level = RES.getString("modification.value.level",loc);
		} else if ("$LEVEL-1".equals(valMod.getRawValue())) {
				level = RES.getString("modification.value.level",loc)+"-1";
		} else if ("$LEVEL*2".equals(valMod.getRawValue())) {
			level = RES.getString("modification.value.level",loc)+"*2";
		} else if ("&INITIATION_RANK".equals(valMod.getRawValue())) {
			level = RES.getString("modification.value.initiation_rank",loc);
		} else
			level = String.valueOf(valMod.getValue());
		return level;
	}

	//-------------------------------------------------------------------
	private static String getModificationStringWithoutCond(Modification mod, Locale loc) {
		logger.log(Level.DEBUG, "explain {0}",mod);
		try {
			ShadowrunReference type = (ShadowrunReference) mod.getReferenceType();
			if (mod instanceof CheckModification) {
				CheckModification valMod = (CheckModification)mod;
				ShadowrunCheckInfluence what = (ShadowrunCheckInfluence) valMod.getWhat();
				String level = getValueString(valMod, loc);
				String prefix = RES.getString("modification.check.for")+" ";
				if (what!=null) {
					switch (what) {
					case DICE  :
						prefix = RES.format("modification.check.dice", level); break;
					case EDGE  :
						prefix = RES.format("modification.check.edge", level); break;
					case EDGE_ONLY_TEST  :
						prefix = RES.format("modification.check.edge_temporary", level); break;
					case EDGE_BOOST:
						prefix = RES.format("modification.check.edge_boost",level); break;
					case EDGE_COST_MALUS:
						prefix = RES.format("modification.check.edge_cost",level); break;
					case NOT_EARN_EDGE:
						prefix = RES.getString("modification.check.not_earn_edge"); break;
					default:
						prefix = "TODO("+what.name()+")";
						prefix+= level;
						logger.log(Level.WARNING, "Not supported: {0}",what);
					}
					prefix+=" "+RES.getString("modification.check.for")+" ";
				}

				switch (type) {
				case ATTRIBUTE:
					String attrName = null;
					if (valMod.getConnectedChoice()!=null) {
						attrName = RES.getString("modification.choice.attribute", loc);
					} else {
						attrName = RES.format("modification.check.attribute", loc, ((ShadowrunAttribute)valMod.getResolvedKey()).getName(loc));
					}
					return prefix+attrName;
				case SKILL:
					String skillName = null;
					if (valMod.getConnectedChoice()!=null) {
						skillName = RES.getString("modification.choice.skill", loc);
					} else if ("ITEM".equals(valMod.getKey())) {
						skillName = RES.getString("modification.skill.item", loc);

					} else {
						SR6Skill skill = Shadowrun6Core.getSkill(valMod.getKey());
						if (skill==null) {
							Logging.logger.log(Level.WARNING, "Found unknown skill '"+valMod.getKey()+"' in valuemod of "+valMod);
							skillName = "Unknown skill '"+valMod.getKey()+"'";
						} else {
							skillName = Shadowrun6Core.getSkill(valMod.getKey()).getName(loc);
						}
					}
					return prefix+skillName;
				case SKILLSPECIALIZATION:
					if (valMod.getConnectedChoice()!=null) {
						skillName = RES.getString("modification.choice.skillspec", loc);
					} else {
						String[] specPair = valMod.getKey().split("/");
						SR6Skill skill = Shadowrun6Core.getSkill(specPair[0]);
						if (skill==null) {
							Logging.logger.log(Level.WARNING, "Found unknown skill '"+specPair[0]+"' in valuemod of "+valMod);
							skillName = "Unknown skill '"+valMod.getKey()+"'";
						} else {
							SkillSpecialization<SR6Skill> spec = skill.getSpecialization(specPair[1]);
							if (spec==null) {
								Logging.logger.log(Level.WARNING, "Found unknown specialization '"+specPair[1]+"' in valuemod of "+valMod);
								skillName = "Unknown skill '"+valMod.getKey()+"'";
							} else {
								skillName = spec.getName(loc);
							}
						}
					}
					return prefix+skillName;
				default:
					logger.log(Level.WARNING, "Don't know how to display "+mod);
					return null;
				}
			}

			if (mod instanceof ValueModification) {
				ValueModification valMod = (ValueModification)mod;
				String level = getValueString(valMod, loc);

				switch (type) {
				case ATTRIBUTE:
					String attrName = null;
					if (valMod.getConnectedChoice()!=null) {
						attrName = RES.getString("modification.choice.attribute", loc);
					} else {
						attrName = ShadowrunAttribute.valueOf(valMod.getKey()).getName(loc);
					}

					if (!valMod.hasFormula() && valMod.getValue()>0) {
						if (valMod.getSet()==ValueType.MAX)
							return attrName+" "+level;
						return attrName+" +"+level;
					} else {
						return attrName+" +"+level;
					}
				case CREATION_POINTS:
					return RES.format("modification.creation_points."+((CreatePoints)valMod.getResolvedKey()).name().toLowerCase(), loc, level);
				case CRITTER_POWER:
					return RES.format("modification.critterpower", loc, ((CritterPower)valMod.getResolvedKey()).getName(loc)+" "+level);
				case HOOK:
					return RES.format("modification.hook.withCap", loc, valMod.getRawValue(),ItemHook.valueOf(valMod.getKey()).getName(loc));
				case ITEM_ATTRIBUTE:
					String iattrName = SR6ItemAttribute.valueOf(valMod.getKey()).getName(loc);
					if (valMod.getFormula().isObject()) {
						return iattrName+" "+valMod.getRawValue();
					} else if (valMod.getFormula().isInteger() || valMod.getFormula().isFloat()) {
						if (valMod.getValueAsDouble()>0) {
							if (valMod.getSet()==ValueType.MAX)
								return iattrName+" "+valMod.getValueAsDouble();
							return iattrName+" +"+valMod.getValueAsDouble();
						} else {
							return iattrName+" "+valMod.getValueAsDouble();
						}
					}
					return iattrName+" +"+level;
				case SKILL:
					String skillName = null;
					if (valMod.getConnectedChoice()!=null) {
						skillName = RES.getString("modification.choice.skill", loc);
					} else {
						SR6Skill skill = Shadowrun6Core.getSkill(valMod.getKey());
						if (skill==null) {
							Logging.logger.log(Level.WARNING, "Found unknown skill '"+valMod.getKey()+"' in valuemod of "+valMod);
							skillName = "Unknown skill '"+valMod.getKey()+"'";
						} else {
							skillName = Shadowrun6Core.getSkill(valMod.getKey()).getName(loc);
						}
					}

					if (valMod.getValue()>0) {
						return skillName+" +"+valMod.getValue();
					} else {
						return skillName+" "+valMod.getValue();
					}
				case GEAR:
					return valMod.getValue()+"x "+((ItemTemplate)valMod.getResolvedKey()).getName(loc);
				case LICENSE:
					return valMod.getValue()+"x "+RES.getString("modification.license", loc)+" "+RES.getString("modification.rating", loc)+" "+((FakeRating)valMod.getResolvedKey()).getValue()+"";
				case QUALITY:
					return valMod.getValue()+"x "+((Quality)valMod.getResolvedKey()).getName(loc);
				case SIN:
					return valMod.getValue()+"x "+RES.getString("modification.sin", loc)+" "+RES.getString("modification.rating", loc)+" "+((FakeRating)valMod.getResolvedKey()).getValue()+"";
				default:
					logger.log(Level.WARNING, "Don't know how to display "+mod);
					return null;
				}
			}

			// AllowModifications
			if (mod instanceof AllowModification) {
				AllowModification valMod = (AllowModification)mod;
				switch (type) {
				case COMPLEX_FORM:
					if (valMod.isNegate()) {
						return RES.format("modification.forbid.complex_form", loc, ((ComplexForm)valMod.getResolvedKey()).getName(loc));
					} else {
						return RES.format("modification.allow.complex_form", loc, ((ComplexForm)valMod.getResolvedKey()).getName(loc));
					}
				case MAGIC_RESO:
					if (valMod.isNegate()) {
						return RES.format("modification.forbid.magicreso", loc, ((MagicOrResonanceType)valMod.getResolvedKey()).getName(loc));
					} else {
						return RES.format("modification.allow.magicreso", loc, ((MagicOrResonanceType)valMod.getResolvedKey()).getName(loc));
					}
				case SKILL:
					if (valMod.isNegate()) {
						if (valMod.getResolvedKey()!=null)
							return RES.format("modification.forbid.skill", loc, ((SR6Skill)valMod.getResolvedKey()).getName(loc));
						else
							return RES.format("modification.forbid.skill_choice", loc, ((SR6Skill)valMod.getResolvedKey()).getName(loc));
					} else {
						if (valMod.getResolvedKey()!=null)
							return RES.format("modification.allow.skill", loc, ((SR6Skill)valMod.getResolvedKey()).getName(loc));
						else
							return RES.format("modification.allow.skill_choice", loc, ((SR6Skill)valMod.getResolvedKey()).getName(loc));
					}
				default:
					logger.log(Level.WARNING, "Don't know how to display "+mod);
					return null;
				}
			}

			//
			if (mod instanceof DataItemModification) {
				DataItemModification valMod = (DataItemModification)mod;
				if (valMod.getKey()!=null && valMod.getKey().startsWith("CHOICE")) {
					return RES.getString("modification.choice."+type.name().toLowerCase(), loc);
				}
				if (valMod.getResolvedKey()==null) {
					if ("CHOICE".equals(valMod.getKey())) {
						return RES.getString("modification.choice."+type.name().toLowerCase(), loc);
					} else {
						logger.log(Level.ERROR, "No resolution for key ''{0}'' in modification {1}", valMod.getKey(), mod);
					}
				}
				switch (type) {
				case CRITTER_POWER:
					return RES.format("modification.critterpower", loc, ((CritterPower)valMod.getResolvedKey()).getName(loc));
				case QUALITY:
					return RES.format("modification.quality", loc, ((Quality)valMod.getResolvedKey()).getName(loc));
				case SKILL:
					return RES.format("modification.skill", loc, ((SR6Skill)valMod.getResolvedKey()).getName(loc));
				case GEAR:
					return ((ItemTemplate)valMod.getResolvedKey()).getName(loc);
				case GEARMOD:
					return ((SR6ItemEnhancement)valMod.getResolvedKey()).getName(loc);
				case HOOK:
					return null;
				case MAGIC_RESO:
					return RES.format("modification.magicreso", loc, ((MagicOrResonanceType)valMod.getResolvedKey()).getName(loc));
				default:
					logger.log(Level.WARNING, "Don't know how to display "+mod);
					return null;
				}
			}

			//
			if (mod instanceof ModificationChoice) {
				ModificationChoice chMod = (ModificationChoice)mod;
				List<String> or = new ArrayList<>();
				chMod.getModificiations().forEach(m -> or.add(getModificationString(m, loc)));
				return String.join(" "+RES.getString("label.or")+" ", or);
			}
			if (mod instanceof RelevanceModification) {
				RelevanceModification rMod = (RelevanceModification)mod;
				switch (rMod.getType()) {
				case "COMBAT": return RES.getString("modification.relevance.combat");
				}
				return RES.format("modification.relevance", rMod.getType());
			}

			logger.log(Level.ERROR, "Don't know how to display "+mod);
			return String.valueOf(mod);
		} catch (Exception e) {
			e.printStackTrace();
			return String.valueOf(mod);
		}
	}

	//-------------------------------------------------------------------
	public static String getModificationString(ComplexDataItem data, Modification mod, Locale loc) {
		ShadowrunReference type = (ShadowrunReference) mod.getReferenceType();
		if (mod instanceof ValueModification) {
			ValueModification valMod = (ValueModification)mod;
			String what = type.name();
			switch (type) {
			case ATTRIBUTE:
				if (valMod.getConnectedChoice()!=null) {
					Choice choice = data.getChoice(valMod.getConnectedChoice());
					if (choice==null) {
						return "Unknown choice "+valMod.getConnectedChoice();
					}
					if (ShadowrunReference.ATTRIBUTE==choice.getChooseFrom()) {
						if (valMod.getValue()>0) {
							return "ein beliebiges Attribut +"+valMod.getValue();
						}
					}
					return "???"+choice.getChooseFrom()+"???";
				}

				if (valMod.getValue()>0) {
					if (valMod.getSet()==ValueType.MAX)
						return ShadowrunAttribute.valueOf(valMod.getKey()).getName(loc)+" "+valMod.getValue();
					return ShadowrunAttribute.valueOf(valMod.getKey()).getName(loc)+" +"+valMod.getValue();
				} else {
					return ShadowrunAttribute.valueOf(valMod.getKey()).getName(loc)+" "+valMod.getValue();
				}
			case ITEM_ATTRIBUTE:
				if (valMod.getConnectedChoice()!=null) {
					Choice choice = data.getChoice(valMod.getConnectedChoice());
					if (choice==null) {
						return "Unknown choice "+valMod.getConnectedChoice();
					}
					if (ShadowrunReference.ATTRIBUTE==choice.getChooseFrom()) {
						if (valMod.getValue()>0) {
							return "ein beliebiges Attribut +"+valMod.getValue();
						}
					}
					return "???"+choice.getChooseFrom()+"???";
				}

				if (valMod.getValue()>0) {
					if (valMod.getSet()==ValueType.MAX)
						return SR6ItemAttribute.valueOf(valMod.getKey()).getName(loc)+" "+valMod.getValue();
					return SR6ItemAttribute.valueOf(valMod.getKey()).getName(loc)+" +"+valMod.getValue();
				} else {
					return SR6ItemAttribute.valueOf(valMod.getKey()).getName(loc)+" "+valMod.getValue();
				}
			case CRITTER_POWER:
				return ((CritterPower)valMod.getResolvedKey()).getName(loc)+" "+valMod.getValue();
			case SKILL:
				if (valMod.getConnectedChoice()!=null) {
					Choice choice = data.getChoice(valMod.getConnectedChoice());
					if (choice==null) {
						return "Unknown choice "+valMod.getConnectedChoice();
					}
					if (ShadowrunReference.SKILL==choice.getChooseFrom()) {
						if (valMod.getAsKeys().length>4) {
							return "ein gewählte Fertigkeit +"+valMod.getValue();
						}
					}
					return "???"+choice.getChooseFrom()+"???";
				}

				SR6Skill skill = Shadowrun6Core.getSkill(valMod.getKey());
				if (skill==null) {
					Logging.logger.log(Level.WARNING, "Found unknown skill '"+valMod.getKey()+"' in valuemod of "+data);
					return "Unknown skill '"+valMod.getKey()+"'";
				}
				if (valMod.getValue()>0) {
					return skill.getName(loc)+" +"+valMod.getValue();
				} else {
					return skill.getName(loc)+" "+valMod.getValue();
				}
			case QUALITY:
				if (valMod.getConnectedChoice()!=null) {
					Logging.logger.log(Level.WARNING, "TODO: value modification for quality with choice: "+valMod);
				}

				Quality quality = Shadowrun6Core.getItem(SR6Quality.class, valMod.getKey());
				if (quality==null) {
					Logging.logger.log(Level.WARNING, "Found unknown quality '"+valMod.getKey()+"' in valuemod of "+data);
					return "Unknown quality '"+valMod.getKey()+"'";
				}
				String prefix = (valMod.isRemove())?"- ":"";
				if (valMod.getValue()>0) {
					return prefix+quality.getName(loc)+" +"+valMod.getValue();
				} else {
					return prefix+quality.getName(loc)+" "+valMod.getValue();
				}
			default:
				return "Unknown value type "+type;
			}
		}

		else if (mod instanceof DataItemModification) {
			DataItemModification valMod = (DataItemModification)mod;
			String what = type.name();
			DataItem resolved = type.resolve(valMod.getKey());

			String prefix = valMod.isRemove()?"- ":"";
			if (resolved!=null) {
				if (valMod.getDecisions().isEmpty())
					return prefix+resolved.getName(loc);
				return prefix+resolved.getName(loc)+"(..)";
			}

//			switch (type) {
//			case QUALITY:
//				Quality qual = Shadowrun6Core.getItem(SR6Quality.class, valMod.getKey());
//				if (qual==null) {
//					return "Unknown quality '"+valMod.getKey()+"'";
//				}
//				if (valMod.getDecisions().isEmpty())
//					return qual.getName(Locale.getDefault());
//				return qual.getName(Locale.getDefault())+"(..)";
//
//			case CRITTER_POWER:
//			}
			logger.log(Level.ERROR, "Not supported yet: Mod without valid reference "+valMod);
			return "ToDo: "+type;
		} else if (mod instanceof ModificationChoice) {
			ModificationChoice choice = (ModificationChoice)mod;
			List<String> or = choice.stream().map(m -> getModificationString(data, m, loc)).collect(Collectors.toList());
			return String.join(" "+RES.getString("label.or")+" ", or);
		}

		Logging.logger.log(Level.ERROR, "Missing string conversion for "+mod.getClass());
		return mod.toString();
	}

	//-------------------------------------------------------------------
	public static Function<Requirement, String> requirementResolver(Locale loc) {
		return (r) -> getRequirementString(r, loc);
	}

	//-------------------------------------------------------------------
	public static String getRequirementString(Requirement req, Locale loc) {
		try {
		if (req instanceof ExistenceRequirement tmp) {
			String prefix = (tmp.isNegate())?(RES.getString("require.negate")+" "):"";
			switch ((ShadowrunReference)tmp.getType()) {
			case GEAR:
				ItemTemplate gear = ShadowrunReference.resolve((ShadowrunReference)tmp.getType(), tmp.getKey());
				if (gear==null)
					return "Unknown "+tmp.getKey();
				return prefix+gear.getName(loc);
			case ITEMTYPE:
				ItemType type = ShadowrunReference.resolve((ShadowrunReference)tmp.getType(), tmp.getKey());
				return prefix+type.getName();
			case ITEMSUBTYPE:
				ItemSubType subtype = ShadowrunReference.resolve((ShadowrunReference)tmp.getType(), tmp.getKey());
				return prefix+subtype.getName();
			case ADEPT_POWER:
				DataItem data2 = ShadowrunReference.resolve((ShadowrunReference)tmp.getType(), tmp.getKey());
				if (data2==null)
					return "Unknown adept power "+tmp.getKey();
				return prefix+ResourceI18N.get(RES, loc, "label.adeptpower")+" "+data2.getName(loc);
			case DRAKE_TYPE:
				data2 = ShadowrunReference.resolve((ShadowrunReference)tmp.getType(), tmp.getKey());
				if (data2==null)
					return "Unknown drake type "+tmp.getKey();
				return prefix+ResourceI18N.get(RES, loc, "label.draketype")+" "+data2.getName(loc);
			case MAGIC_RESO:
			case MARTIAL_ART:
			case METATYPE:
			case QUALITY:
			case COMPLEX_FORM:
			case SPELL:
			case TECHNIQUE:
				DataItem data = ShadowrunReference.resolve((ShadowrunReference)tmp.getType(), tmp.getKey());
				if (data==null)
					return "Unknown "+tmp.getKey();
				return prefix+data.getName(loc);
			case METAECHO:
				data = ShadowrunReference.resolve((ShadowrunReference)tmp.getType(), tmp.getKey());
				if (data==null)
					return "Unknown "+tmp.getKey();
				String meType = "";
				switch ( ((MetamagicOrEcho)data).getType() ) {
				case ECHO: meType = RES.getString("label.echo"); break;
				case METAMAGIC: meType = RES.getString("label.metamagic"); break;
				case METAMAGIC_ADEPT: meType = RES.getString("label.metamagic"); break;
				case DRACOGENESIS_POWER: meType = RES.getString("label.dracogenesis"); break;
				}
				return prefix+meType+" "+data.getName(loc);
			case SKILL:
				String value = (req instanceof ValueRequirement)?((ValueRequirement)req).getRawValue():"";
				if ("CHOICE".equals(tmp.getKey())) {
					if (tmp.isNegate())
						return ResourceI18N.format(RES, loc, "skill.chosen.not", value);
					return ResourceI18N.format(RES, loc, "skill.chosen", value);
				}
				SR6Skill skill = Shadowrun6Core.getItem(SR6Skill.class, tmp.getKey());
				if (skill==null)
					return "Unknown "+tmp.getKey();
				return prefix+skill.getName(loc);
			case SKILLSPECIALIZATION:
				data = ShadowrunReference.resolve((ShadowrunReference)tmp.getType(), tmp.getKey());
				if (data==null)
					return "Unknown "+tmp.getKey();
				return prefix+"SkillSpec "+" "+data.getName(loc);
//			case MASTERSHIP:
//				Mastership master =  SplitterMondCore.getItem(Mastership.class, tmp.getKey());
//				if (master==null) {
//					return "Unknown "+SplitterMondCore.getI18nResources().getString("label.mastership")+" "+tmp.getKey();
//				}
//				return master.getName(loc);
			default:
				System.err.println("Shadowrun6Tools: Making cleartext of "+tmp.getType()+" existance req. not supported");
				Logging.logger.log(Level.ERROR, "Making cleartext of "+tmp.getType()+" existance req. not supported");
			}
		} else if (req instanceof ValueRequirement) {
			ValueRequirement tmp = (ValueRequirement)req;
			ShadowrunReference type = (ShadowrunReference)tmp.getType();
			DataItem item = null;
			switch ((ShadowrunReference)tmp.getType()) {
			case ATTRIBUTE:
				if (tmp.getMaxValue()>0) {
					if (tmp.getMaxValue()>=100) {
						return ShadowrunAttribute.valueOf(req.getKey()).getName(loc)+" <="+( ((double)tmp.getMaxValue()/1000));
					}
					return ShadowrunAttribute.valueOf(req.getKey()).getName(loc)+" <="+tmp.getMaxValue();
				}
				if ("$RATING".equals(tmp.getRawValue())) {
					return ShadowrunAttribute.valueOf(req.getKey()).getName(loc)+" >= "+RES.getString("label.rating", loc);
				}
				return ShadowrunAttribute.valueOf(req.getKey()).getName(loc)+" "+tmp.getValue()+"+";
			case ITEM_ATTRIBUTE:
				SR6ItemAttribute iAttr = SR6ItemAttribute.valueOf(tmp.getKey());
				if (iAttr==SR6ItemAttribute.ITEMTYPE) return ItemType.valueOf(tmp.getRawValue()).getName();
				if (iAttr==SR6ItemAttribute.ITEMSUBTYPE) return ItemSubType.valueOf(tmp.getRawValue()).getName();
				if (iAttr==SR6ItemAttribute.WEAPON_SIZE) return WeaponSize.valueOf(tmp.getRawValue()).getName();
				if (iAttr==SR6ItemAttribute.SOFTWARE_TYPES) return "Device runs "+tmp.getRawValue();
				return iAttr.getName(loc)+" "+tmp.getRawValue()+"+";
			case SKILL:
				if (tmp.getChoice()!=null) {
					return RES.format("requirement.choice.skill", loc, getValueString(tmp, loc));
				}
				item = ShadowrunReference.resolve(type, req.getKey());
				return item.getName(loc)+" "+tmp.getRawValue()+"+";
			case ADEPT_POWER:
			case METAECHO:
			case QUALITY:
			case TECHNIQUE:
				DataItem qual = ShadowrunReference.resolve((ShadowrunReference)tmp.getType(), tmp.getKey());
				if (qual==null)
					return "Unknown "+tmp.getKey();
				return qual.getName(loc);
			}
		} else if (req instanceof AnyRequirement) {
			AnyRequirement any = (AnyRequirement)req;
			List<String> names = any.getOptionList().stream().map(r -> getRequirementString(r,loc)).collect(Collectors.toList());
			return "("+String.join(", ", names)+")";
		}

		Logging.logger.log(Level.ERROR, "Missing string conversion for "+req.getClass()+" and "+req.getType());
		System.err.println("Shadowrun6Tool: Missing string conversion for "+req.getClass()+" and "+req.getType());
		return req.toString();
		} catch (Exception e) {
			logger.log(Level.ERROR, "Error converting to String: "+req,e);
			return "ERROR: "+req;
		}
	}

	//-------------------------------------------------------------------
	/**
	 * Walk through all items in the character and resolve them
	 * @param rawChar
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void resolveChar(Shadowrun6Character model) {
		logger.log(Level.INFO, "ENTER resolveChar({0}) with {1} Karma", model.getName(), model.getKarmaFree());
		try {
			logger.log(Level.DEBUG, "resolve qualities");
			for (QualityValue tmp : model.getQualities()) {
				tmp.setCharacter(model);
				Quality resolved = Shadowrun6Core.getItem(SR6Quality.class, tmp.getKey(), tmp.getLanguage());
				tmp.setResolved(resolved);
			}
			logger.log(Level.DEBUG, "resolve shifter qualities");
			for (QualityValue tmp : model.getShifterAddOns()) {
				tmp.setCharacter(model);
				Quality resolved = Shadowrun6Core.getItem(SR6Quality.class, tmp.getKey(), tmp.getLanguage());
				tmp.setResolved(resolved);
			}

			logger.log(Level.DEBUG, "resolve quality paths");
			for (QualityPathValue tmp : model.getQualityPaths()) {
				tmp.setCharacter(model);
				QualityPath resolved = Shadowrun6Core.getItem(QualityPath.class, tmp.getKey(), tmp.getLanguage());
				if (resolved==null) {
					logger.log(Level.ERROR, "Character {0} contains unknown quality path '{1}'", model.getName(), tmp.getKey());
					continue;
				}
				tmp.setResolved(resolved);

				for (QualityPathStepValue v : tmp.getStepsTaken()) {
					QualityPathStep resolved2 = resolved.getStep(v.getKey());
					if (resolved2==null) logger.log(Level.ERROR, "Character {0} contains unknown quality path step '{1}'", model.getName(), tmp.getKey());
					v.setResolved(resolved2);
				}
			}

			logger.log(Level.DEBUG, "resolve skills");
			for (SR6SkillValue tmp : model.getSkillValues()) {
				SR6Skill resolved = Shadowrun6Core.getItem(SR6Skill.class, tmp.getKey(), tmp.getLanguage());
				if (resolved==null) logger.log(Level.ERROR, "Character {0} contains unknown skill '{1}'", model.getName(), tmp.getKey());
				tmp.setResolved(resolved);
				// Specs
				for (SkillSpecializationValue<SR6Skill> v : tmp.getSpecializations()) {
					if (v.getResolved()==null) {
						SkillSpecialization<SR6Skill> spec = resolved.getSpecialization(v.getKey());
						if (spec==null) {
							logger.log(Level.ERROR, "Character {} contains unknown skill specialization '{}'", model.getName(), v.getKey());
						} else {
							v.setResolved(spec);
						}
					}
				}
			}
			logger.log(Level.DEBUG, "resolve adept powers");
			for (AdeptPowerValue tmp : model.getAdeptPowers()) {
				tmp.setCharacter(model);
				AdeptPower resolved = Shadowrun6Core.getItem(AdeptPower.class, tmp.getKey(), tmp.getLanguage());
				tmp.setResolved(resolved);
			}
			logger.log(Level.DEBUG, "resolve spells");
			for (SpellValue tmp : model.getSpells()) {
				tmp.setCharacter(model);
				SR6Spell resolved = Shadowrun6Core.getItem(SR6Spell.class, tmp.getKey(), tmp.getLanguage());
				tmp.setResolved(resolved);
			}
			logger.log(Level.DEBUG, "resolve rituals");
			for (RitualValue tmp : model.getRituals()) {
				tmp.setCharacter(model);
				Ritual resolved = Shadowrun6Core.getItem(Ritual.class, tmp.getKey(), tmp.getLanguage());
				tmp.setResolved(resolved);
			}
			logger.log(Level.DEBUG, "resolve complex forms");
			for (ComplexFormValue tmp : model.getComplexForms()) {
				tmp.setCharacter(model);
				ComplexForm resolved = Shadowrun6Core.getItem(ComplexForm.class, tmp.getKey(), tmp.getLanguage());
				tmp.setResolved(resolved);
			}
			logger.log(Level.DEBUG, "resolve metamagics or echoes");
			for (MetamagicOrEchoValue tmp : model.getMetamagicOrEchoes()) {
				tmp.setCharacter(model);
				MetamagicOrEcho resolved = Shadowrun6Core.getItem(MetamagicOrEcho.class, tmp.getKey(), tmp.getLanguage());
				tmp.setResolved(resolved);
			}
			logger.log(Level.DEBUG, "resolve critter powers");
			for (CritterPowerValue tmp : model.getCritterPowers()) {
				tmp.setCharacter(model);
				CritterPower resolved = Shadowrun6Core.getItem(CritterPower.class, tmp.getKey(), tmp.getLanguage());
				tmp.setResolved(resolved);
			}
			logger.log(Level.DEBUG, "resolve lifestyles");
			for (SR6Lifestyle tmp : model.getLifestyles()) {
				tmp.setCharacter(model);
				LifestyleQuality resolved = Shadowrun6Core.getItem(LifestyleQuality.class, tmp.getKey(), tmp.getLanguage());
				tmp.setResolved(resolved);
			}

			logger.log(Level.DEBUG, "resolve gear");
			SR6ResolveTemplatesStep resolver = new SR6ResolveTemplatesStep();
			for (CarriedItem<ItemTemplate> tmp : model.getCarriedItems()) {
				tmp.setCharacter(model);
				if (tmp.getUuid()==null)
					logger.log(Level.WARNING, "Char {0} Item {1} has no UUID", model.getName(), tmp.getKey());
				if (tmp.getResolved()==null) {
					ItemTemplate resolved = Shadowrun6Core.getItem(ItemTemplate.class, tmp.getKey(), tmp.getLanguage());
					if (resolved==null && tmp.getKey().equals("software_library"))
						resolved = ItemUtil.SOFTWARE_LIBRARY_ITEM;
					if (resolved==null) {
						logger.log(Level.ERROR, "Char {0} has an unresolvable item: {1}", model.getName(), tmp.getKey());
						System.err.println("Char "+model.getName()+" has an unresolvable item: "+tmp.getKey());
						model.removeCarriedItem(tmp);
					} else {
					tmp.setResolved(resolved);
						if (tmp.getVariantID()!=null && tmp.getVariant()==null) {
							tmp.setVariant( tmp.getResolved().getVariant(tmp.getCarryMode()) );
						}
					}
				}
				for (ItemEnhancementValue<AItemEnhancement> eVal : tmp.getEnhancements()) {
					eVal.setResolved( Shadowrun6Core.getItem(SR6ItemEnhancement.class, eVal.getKey(), eVal.getLanguage()));
				}
				resolver.process(false, ShadowrunReference.ITEM_ATTRIBUTE, model, tmp, List.of());
				SR6GearTool.recalculate("", model, tmp);
				tmp.setDirty(false);
			}

			logger.log(Level.DEBUG, "resolve martial arts");
			for (MartialArtsValue tmp : model.getMartialArts()) {
				tmp.setCharacter(model);
				MartialArts resolved = Shadowrun6Core.getItem(MartialArts.class, tmp.getKey(), tmp.getKey());
				tmp.setResolved(resolved);
				Technique resolved2 = Shadowrun6Core.getItem(Technique.class, resolved.getSignatureTechniqueID());
				if (resolved2==null) {
					logger.log(Level.ERROR, "Failed resolving reference {1} ''{2}'' in character {0}", model.getName(), ShadowrunReference.MARTIAL_ART, resolved.getSignatureTechniqueID(), null);
					BabylonEventBus.fireEvent(BabylonEventType.UI_MESSAGE, 1, "Data error for martial art "+resolved);
				}

			}

			logger.log(Level.DEBUG, "resolve martial arts techniques");
			for (TechniqueValue tmp : model.getTechniquesAll()) {
				tmp.setCharacter(model);
				Technique resolved = Shadowrun6Core.getItem(Technique.class, tmp.getKey(), tmp.getKey());
				tmp.setResolved(resolved);
			}

			logger.log(Level.DEBUG, "resolve foci");
			for (FocusValue tmp : model.getFoci()) {
				tmp.setCharacter(model);
				Focus resolved = Shadowrun6Core.getItem(Focus.class, tmp.getKey(), tmp.getKey());
				tmp.setResolved(resolved);
			}

			logger.log(Level.DEBUG, "resolve data structures");
			for (DataStructureValue tmp : model.getDataStructures()) {
				tmp.setCharacter(model);
				DataStructure resolved = Shadowrun6Core.getItem(DataStructure.class, tmp.getKey(), tmp.getKey());
				tmp.setResolved(resolved);
			}

			logger.log(Level.DEBUG, "resolve SURGE collective");
			SetItemValue collect = model.getSurgeCollective();
			if (collect!=null) {
				SetItem resolved = Shadowrun6Core.getItem(SetItem.class, collect.getKey(), collect.getKey());
				collect.setResolved(resolved);
			}

			logger.log(Level.DEBUG, "resolve drake type");
			if (model.getDrakeType()!=null) {
				DrakeType resolved =  Shadowrun6Core.getItem(DrakeType.class, model.getDrakeType().getKey());
				if (resolved==null) {
					logger.log(Level.ERROR, "Cannot resolve drake type ''%s'' in character ''{1}''", model.getDrakeType().getKey(), model.getName());
					System.err.println("Cannot resolve drake type '"+model.getDrakeType().getKey()+"'");
					BabylonEventBus.fireEvent(BabylonEventType.UI_MESSAGE, 1, "Cannot resolve drake type '"+model.getDrakeType().getKey()+"' for "+model.getName());
				}
				model.getDrakeType().setResolved(resolved);
			}
		} catch (DataErrorException e) {
			logger.log(Level.ERROR, "Failed resolving reference {1} ''{2}'' in character {0}", model.getName(), e.getReferenceError().getType(), e.getReferenceError().getReference(), e);
		} catch (Exception e) {
			logger.log(Level.ERROR, "Failed resolving references in character {0}", model.getName(),e);
		} finally {
			logger.log(Level.DEBUG, "LEAVE resolveChar");
		}
	}

	//-------------------------------------------------------------------
	public static boolean isRequirementMet(Shadowrun6Character model, ComplexDataItem requiredFor, Requirement req, Decision[] decisions) {
		if (req.getApply()!=null && req.getApply()!=ApplyTo.CHARACTER) return true;

		try {

		if (req instanceof ExistenceRequirement) {
			ExistenceRequirement tmp = (ExistenceRequirement)req;
			boolean negated = tmp.isNegate();
			ShadowrunReference type = (ShadowrunReference)tmp.getType();
			Object foo = null;
			if (type!=ShadowrunReference.CARRIED)
				foo = ShadowrunReference.resolve(type, req.getKey());
			if (!(foo instanceof DataItem)) {
				logger.log(Level.WARNING, "Character requirement check not implemented for {0} and foo={1}",type, foo);
				return true;
			}
			DataItem item = ShadowrunReference.resolve(type, req.getKey());
			switch (type) {
			case ADEPT_POWER:
				if (negated) return !model.hasAdeptPower(req.getKey());
				return model.hasAdeptPower(req.getKey());
			case COMPLEX_FORM:
				if (negated) return !model.hasComplexForm(req.getKey());
				return model.hasComplexForm(req.getKey());
			case DRAKE_TYPE:
				if (negated) return model.getDrakeType()==null || item!=model.getDrakeType().getResolved();
				return model.getDrakeType()!=null || item==model.getDrakeType().getResolved();
			case QUALITY:
				if (negated) return !model.hasQuality(req.getKey());
				return model.hasQuality(req.getKey());
			case METATYPE:
				if (model.getMetatype()==null) return false;
				if (negated) {
					return !model.getMetatype().getId().equals(req.getKey());
				} else {
					if (model.getMetatype().getId().equals(req.getKey())) return true;
					if (model.getMetatype().getVariantOf()!=null && model.getMetatype().getVariantOf().getId().equals(req.getKey())) return true;
				}
				return false;
			case MAGIC_RESO:
				return model.getMagicOrResonanceType()!=null && model.getMagicOrResonanceType()==item;
			case METAECHO:
				if (negated) return !model.hasMetamagicOrEcho(req.getKey());
				return model.hasMetamagicOrEcho(req.getKey());
//			case SPELL:
//				if (negated) return !model.hasSpell(req.getKey());
//				return model.hasSpell(req.getKey());
			case GEAR:
			case CARRIED:
				// Character needs to have a specific gear
				for (CarriedItem<ItemTemplate> gear : model.getCarriedItems()) {
					if (gear.getModifyable().getId().equals(req.getKey())) {
						return true;
					}
				}
				return false;
			default:
				System.err.println("Shadowrun6Tool: Todo: isRequirementMet for "+type+" = "+item);
				logger.log(Level.WARNING, "Todo: isRequirementMet for "+type+" = "+item);
			}
			return false;
		} else if (req instanceof AnyRequirement) {
			AnyRequirement any = (AnyRequirement)req;
			for (Requirement tmp : any.getOptionList()) {
				if (isRequirementMet(model, requiredFor, tmp, decisions))
					return true;
			}
			return false;
		} else if (req instanceof ValueRequirement) {
			ValueRequirement tmp = (ValueRequirement)req;
			ShadowrunReference type = (ShadowrunReference)tmp.getType();
//			switch (type) {
//			case ITEM_ATTRIBUTE:
//				SR6ItemAttribute itemAttr = SR6ItemAttribute.valueOf(tmp.getKey());
//				switch (itemAttr) {
//				case ITEMTYPE:
//					ItemType iType = ItemType.valueOf(tmp.getRawValue());
//				}
//			}
			int min = -1;
			int max = Integer.MAX_VALUE;
			if (tmp.getFormula().isResolved() && tmp.getFormula().isInteger()) {
				if (tmp.getRawValue()!=null) {
					min = tmp.getValue();
				} else {
					max = tmp.getMaxValue();
				}
			} else {
				if (requiredFor.getClass()==ItemTemplate.class) {
					List<Usage> usages = ((ItemTemplate) requiredFor).getUsages();
					if (usages.isEmpty()) { 
						logger.log(Level.ERROR, "No usages for "+requiredFor+" for value req "+tmp); 
					}
					CarryMode mode = usages.isEmpty()?CarryMode.EMBEDDED:usages.get(0).getMode();
					CarriedItem item = GearTool.buildItem((ItemTemplate) requiredFor, mode, model, false, decisions).get();
					VariableResolver resolver = new VariableResolver(item, model);
					if (((FormulaImpl)tmp.getFormula()).getAsString().startsWith("$")) {
						logger.log(Level.WARNING, "ToDo: Resolve "+tmp.getFormula());
						SR6ItemAttribute itemAttr = SR6ItemAttribute.valueOf( ((FormulaImpl)tmp.getFormula()).getAsString().substring(1));
						String raw = FormulaTool.resolve(ShadowrunReference.ITEM_ATTRIBUTE, (FormulaImpl)tmp.getFormula(), resolver);
						min = Integer.valueOf(raw);
					}
				} else
					logger.log(Level.WARNING, "ToDo: check unresolved requirement "+req.getKey()+":"+tmp.getFormula()+" for "+requiredFor.getClass());
			}
			Object item = ShadowrunReference.resolve(type, req.getKey());
			if (item==null && !("CHOICE".equals(req.getKey()))) {
				logger.log(Level.ERROR, "Cannot find item for key ''{0}''", tmp.getType()+":"+tmp.getKey());
				return false;
			}
			switch (type) {
			case SKILL:
				if ("CHOICE".equals(tmp.getKey())) {
					return true;
				}
				if (model.getSkillValue((SR6Skill)item)==null) {
					return false;
				}
				int val = model.getSkillValue( (SR6Skill)item).getModifiedValue();
				if (max!=Integer.MAX_VALUE && val>max) return false;
				if (min>0 && val<min) return false;
				//if (max>0 && val>min) return false;
				return true;
			case ATTRIBUTE:
				if ("CHOICE".equals(tmp.getKey())) {
					return true;
				}
				if (model.getAttribute((ShadowrunAttribute)item)==null) {
					return false;
				}
				val = model.getAttribute( (ShadowrunAttribute)item).getModifiedValue();
				if (max!=Integer.MAX_VALUE && val>max) return false;
				if (min>0 && val<min) return false;
				//if (max>0 && val>min) return false;
				return true;
			case ITEM_ATTRIBUTE:
				if ("CHOICE".equals(tmp.getKey())) {
					return true;
				}
				SR6ItemAttribute iAttr = tmp.getType().resolve(tmp.getKey());
				if (requiredFor instanceof ItemTemplate) {
					ItemAttributeDefinition def = ((ItemTemplate)requiredFor).getAttribute(iAttr);
					if (def==null) return false;
					switch (iAttr) {
					case ITEMSUBTYPE:
					case ITEMTYPE:
						return tmp.getRawValue().equals(def.getRawValue());
					default:
						logger.log(Level.WARNING, "TODO: Compare "+tmp.getRawValue()+" with "+def.getRawValue());
					}
				} else {
					logger.log(Level.WARNING, "Don't know how to check item attribute "+tmp+" for "+requiredFor);
					return false;
				}
				return false;
			case QUALITY:
				if ("CHOICE".equals(tmp.getKey())) {
					return true;
				}
				if (model.getQuality(tmp.getKey())==null) {
					return false;
				}
				val = model.getQuality(tmp.getKey()).getModifiedValue();
				if (max!=Integer.MAX_VALUE && val>max) return false;
				if (min>0 && val<min) return false;
				//if (max>0 && val>min) return false;
				return true;
			default:
				logger.log(Level.WARNING, "Todo: isRequirementMet for "+type);
			}
		}
		System.err.println("Shadowrun6Tool: Requirement checking not supported for "+req.getClass()+" and "+req.getType());
		logger.log(Level.WARNING,"ToDo: Requirement checking not supported for "+req.getClass()+" and "+req.getType());
		return false;

		} catch (Exception e) {
			logger.log(Level.ERROR, "Error checking requirement "+req+" from "+requiredFor,e);
			e.printStackTrace();
		}
		return false;
	}

	//-------------------------------------------------------------------
	public static Possible areRequirementsMet(Shadowrun6Character model, ComplexDataItem data, Decision[] decisions) {
		List<Requirement> list = new ArrayList<>();
		for (Requirement req : data.getRequirements()) {
			if (req.getApply()!=null && req.getApply()!=ApplyTo.CHARACTER)
				continue;
			if (!isRequirementMet(model, data, req, decisions)) {
				list.add(req);
			}
		}

		if (list.isEmpty())
			return Possible.TRUE;
		return new Possible(list.toArray(new Requirement[list.size()]));
	}

	//-------------------------------------------------------------------
	public static Possible checkDecisionsAndRequirements(Shadowrun6Character model, ComplexDataItem data, Decision...decisions) {
		Possible p1 = areRequirementsMet(model, data, decisions);
		Possible p2 = GenericRPGTools.areAllDecisionsPresent(data, decisions);

		return new Possible(p1, p2);
	}

	//-------------------------------------------------------------------
	public static Modification instantiateModification(Modification tmp, ComplexDataItemValue<?> value, int multiplier, Shadowrun6Character model) {
		logger.log(Level.DEBUG, "instantiate {0} with multiplier {1}",tmp,multiplier);
		if (tmp instanceof ValueModification vMod) {
			if (vMod.isInstantiated()) {
				return vMod;
			}
			ValueModification clone = ((ValueModification)tmp).clone();			
			clone.setApplyTo(tmp.getApplyTo());
			int modVal = 0;
			if (clone.hasFormula()) {
				logger.log(Level.DEBUG, "Found a formula :(  "+clone.getFormula());
				logger.log(Level.DEBUG, "  model =  "+model);
				logger.log(Level.DEBUG, "  data item =  "+value.getKey());
				logger.log(Level.DEBUG, "  data item value =  "+value);
				String resolved = FormulaTool.resolve(clone.getReferenceType(), clone.getFormula(), new VariableResolver(value, model));
				logger.log(Level.DEBUG, "  {0} resolved as {1}", clone.getFormula(),resolved);
				modVal = Integer.parseInt(resolved);
				clone.setValue(modVal);
			} else {
				if (clone.getLookupTable()!=null) {
					modVal = clone.getValue();
					if (modVal>clone.getLookupTable().length) {
						logger.log(Level.ERROR, "Modification {0}, multiplier {1} is outside table", tmp, multiplier);
					}
					clone.setValue( clone.getLookupTable()[modVal-1] );
					clone.setLookupTable(null);
				} else if (multiplier>1) {
					if (clone.isDouble()) {
						double newVal = clone.getValueAsDouble()*multiplier;
						clone.setRawValue( String.valueOf( newVal ));
					} else {
						modVal = clone.getValue();
						clone.setRawValue( String.valueOf( modVal*multiplier ));
						clone.setValue( modVal*multiplier );
					}
				}
			}
			if ("CHOICE".equals( clone.getKey() )) {
				UUID uuid =  ((ValueModification) tmp).getConnectedChoice();
				Decision dec = value.getDecision(uuid);
				if (dec!=null) {
					clone.setKey( dec.getValue());
				} else {
					logger.log(Level.ERROR, "No decision for {0} found in {1}", uuid, value.getKey());
					System.err.println("Shadowrun6Tools.instantiate: No decision for "+uuid+" found in "+value);
					try {
						throw new RuntimeException("Trace");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					BabylonEventBus.fireEvent(BabylonEventType.UI_MESSAGE, 2, "Shadowrun6Tools.instantiate: No decision for "+uuid+" found in "+value.getKey());
					clone.setKey(null);
				}
			} else if ("ITEM".equals( clone.getKey() )) {
				// Get the connected item UUID
				String source = String.valueOf(value.getModifyable());
				Choice carriedChoice = value.getModifyable().getChoice(ShadowrunReference.CARRIED);
				if (carriedChoice==null) {
					logger.log(Level.ERROR, "Trying to instantiate a ref=ITEM modification, but the {0} has no CARRIED choice",source);
				} else {
					Decision dec = value.getDecision(carriedChoice.getUUID());
					if (dec==null) {
						logger.log(Level.ERROR, "Trying to instantiate a ref=ITEM modification, but the decision for {0} is missing in {1}",carriedChoice.getUUID(), value);
					} else {
						UUID connectedItemUUID = UUID.fromString(dec.getValue());
						CarriedItem<ItemTemplate> item = model.getCarriedItem(connectedItemUUID);
						// No act depending on what is needed from referenced item
						switch ((ShadowrunReference)tmp.getReferenceType()) {
						case SKILL:
							ItemAttributeObjectValue<SR6ItemAttribute> attrObj = item.getAsObject(SR6ItemAttribute.SKILL);
							if (attrObj==null) {
								logger.log(Level.ERROR, "No SKILL item attribute in carried item {0}", item);
							} else {
								SR6Skill skill = attrObj.getModifiedValue();
								logger.log(Level.INFO, "Set skill {0} in instantiated {1}", skill.getId(), tmp);
								clone.setKey(skill.getId());
								logger.log(Level.INFO, "Add modification {0} to {1}", clone, item);
								item.addOutgoingModification(clone);
								return null;
							}
							break;
						default:
							logger.log(Level.ERROR, "ToDo: Implement fetching {0} from CarriedItem",tmp.getReferenceType());
							System.err.println("ToDo: Implement fetching "+tmp.getReferenceType()+" from CarriedItem");
						}
					}
				}
			}
			if ("$LEVEL".equals(clone.getRawValue())) {
				logger.log(Level.DEBUG, "Replace $LEVEL with {0}", multiplier);
				clone.setValue(multiplier);
			}

			// Copy all decisions
			if (clone.getResolvedKey() instanceof ComplexDataItem) {
				List<UUID> expected = ((ComplexDataItem)clone.getResolvedKey()).getChoices()
						.stream()
						.map( c -> c.getUUID() )
						.collect(Collectors.toList())
						;
				if (!expected.isEmpty()) {
					List<Decision> accepted = value.getDecisions().stream().filter( d -> expected.contains(d.getChoiceUUID()) ).collect(Collectors.toList());
					logger.log(Level.INFO, "Decisions from {0} that are added to instantiaeted modiciation: {1}", value.getKey(),accepted);
					clone.setDecisions(accepted);
				}
			}

			return clone;
		}
		if (tmp instanceof DataItemModification) {
			DataItemModification clone = ((DataItemModification)tmp).clone();
			if ("CHOICE".equals( clone.getKey() )) {
				UUID uuid =  ((DataItemModification) tmp).getConnectedChoice();
				Decision dec = value.getDecision(uuid);
				logger.log(Level.DEBUG, "instantiate {2} with UUID {0} and decision {1}", uuid,dec,clone.getKey());
				if (dec!=null) {
					clone.setKey( dec.getValue());
				} else {
					logger.log(Level.ERROR, "No decision for {0} found in {1}", uuid, value);
				}
			}

			return clone;
		}
		if (tmp instanceof RelevanceModification) {
			return tmp;
		}

		throw new IllegalArgumentException("Cannot instantiate "+tmp.getClass());
	}

	//-------------------------------------------------------------------
	public static String getAttackRatingString(int[] attackRating) {
		if (attackRating==null) return "ERROR";
		String[] ratings = new String[attackRating.length];
		for (int i=0; i<ratings.length; i++) {
			int val = attackRating[i];
			if (val!=0)
				ratings[i] = String.valueOf(val);
			else
				ratings[i] = "-";
		}
		return String.join("/", ratings);
	}

	//-------------------------------------------------------------------
	/**
	 * Give the advantages of this item and non-recursively walk through all
	 * embedded items and return their advantages as well
	 */
	public static Collection<String> getWiFiAdvantageStrings(CarriedItem<ItemTemplate> carried, Locale locale) {
		List<String> ret = new ArrayList<>();
		ret.addAll( carried.getResolved().getWiFiAdvantageStrings(carried.getCarryMode(), carried.getVariant(), locale) );
		return ret;
	}

	//-------------------------------------------------------------------
	public static boolean isType(CarriedItem<ItemTemplate> item, ItemType ... types) {
		ItemType found = item.getAsObject(SR6ItemAttribute.ITEMTYPE).getModifiedValue();
		for (ItemType tmp : types) {
			if (tmp==found) return true;
		}
		return false;
	}

	//-------------------------------------------------------------------
	public static boolean isSubtype(CarriedItem<ItemTemplate> item, ItemSubType ... types) {
		ItemSubType found = item.getAsObject(SR6ItemAttribute.ITEMSUBTYPE).getModifiedValue();
		for (ItemSubType tmp : types) {
			if (tmp==found) return true;
		}
		return false;
	}

	//-------------------------------------------------------------------
	public static List<CarriedItem<ItemTemplate>> getMatrixItems(Shadowrun6Character model) {
		return model.getCarriedItems()
				.stream()
				.filter(ci -> ci.hasFlag(SR6ItemFlag.MATRIX_DEVICE.name()))
				.collect(Collectors.toList());

	}

	//--------------------------------------------------------------------
	/**
	 * @param skill
	 * @param useAttrib  Attribute to use
	 * @param special IDs of specializations to use (only use highest)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Pool<Integer> getSkillPoolCalculationWithoutAttribute(Shadowrun6Character model, SR6Skill skill, String...special) {
		Pool<Integer> ret = new Pool<Integer>();

		// Add the unmodified skill
		SR6SkillValue     sVal = model.getSkillValue(skill);
		if (sVal==null) {
			// Skill not present
			if (!skill.isUseUntrained()) {
				RES.format("explain.skill_not_untrained", skill.getName());
				ret.addStep(ValueType.NATURAL, new PoolCalculation<Integer>(0, RES.format("explain.skill_not_untrained", skill.getName())));
				return ret;
			} else {
				ret.addStep(ValueType.NATURAL, new PoolCalculation<Integer>(-1, RES.format("explain.untrained_skill", skill.getName())));
			}
		} else {
			ret.addStep(ValueType.NATURAL, new PoolCalculation<Integer>(sVal.getDistributed(), Shadowrun6Core.getI18nResources().format( "explain.skillpoints", skill.getName())));
//			if (sVal.getAlternativePoints()>sVal.getDistributed()) {
//				ret.clear();
//				ret.add(new PoolCalculation(sVal.getAlternativePoints(), Resource.format(ShadowrunCore.getI18nResources(), "explain.skillpoints.alternative",
//						skill.getName(),
//						ShadowrunTools.getModificationSourceString(sVal.getAlternativeSource()))));
//			}
			// Now add modifiers from the skill
			int augAllowed = 4;
			for (Modification mod : sVal.getIncomingModifications()) {
				if (mod.getReferenceType()==ShadowrunReference.SKILL && mod instanceof ValueModification) {
					ValueModification sMod = (ValueModification)mod;
					if (sMod.getResolvedKey()==skill && !sMod.isConditional() && sMod.getSet()!=ValueType.ARTIFICIAL && sMod.getSet()!=ValueType.MAX) {
						int val = Math.min(augAllowed, sMod.getValue());
						// Mark modifiers being capped with augmentation limit
						PoolCalculation<Integer> calc = new PoolCalculation<Integer>(val, Shadowrun6Tools.getModificationSourceString(sMod.getSource()));
						calc.hitLimit = val<sMod.getValue();
						ret.addStep(ValueType.AUGMENTED, calc);
						augAllowed -= val;
					}
				}
			}

			// Now specializations
			addSpecialization(ret, sVal, special);
		}

		return ret;
	}

	//--------------------------------------------------------------------
	public static Pool<Integer> getSkillPool(Shadowrun6Character model, SR6Skill skill, String...special) {
		SkillSpecializationValue<SR6Skill> spec = getBestSpecialization(model.getSkillValue(skill), special);
		ShadowrunAttribute attrib = (spec!=null && spec.getResolved().getAttribute()!=null)?(ShadowrunAttribute.valueOf(spec.getResolved().getAttribute())):skill.getAttribute();
		return getSkillPool(model, skill, attrib, special);
	}

	//-------------------------------------------------------------------
	private static SkillSpecializationValue<SR6Skill> getBestSpecialization(SR6SkillValue sVal, String...special) {
		SkillSpecializationValue<SR6Skill> bestSpec = null;
		if (sVal==null) return null;
		for (SkillSpecializationValue<SR6Skill> spec : sVal.getSpecializations()) {
			// Test if specializ. matches requested specs
			if (!Arrays.asList(special).contains(spec.getResolved().getId()))
				continue;
			//if (bestSpec == null || spec.isExpertise())
				bestSpec = spec;
		}
		return bestSpec;
	}

	//--------------------------------------------------------------------
	/**
	 * @param skill
	 * @param useAttrib  Attribute to use
	 * @param special IDs of specializations to use (only use highest)
	 * @return
	 */
	public static Pool<Integer> getSkillPool(Shadowrun6Character model, SR6Skill skill, ShadowrunAttribute useAttrib, String...special) {
		Pool<Integer> ret = getSkillPoolCalculationWithoutAttribute(model, skill, special);
		
		SkillSpecializationValue<SR6Skill> spec = getBestSpecialization(model.getSkillValue(skill), special);
		ShadowrunAttribute attrib = (spec!=null && spec.getResolved().getAttribute()!=null)?(ShadowrunAttribute.valueOf(spec.getResolved().getAttribute())):skill.getAttribute();
		if (useAttrib!=null)
			attrib = useAttrib;
		
		ret.addAll(
				model.getAttribute( attrib).getPool()
				);
		return ret;
	}

	//-------------------------------------------------------------------
	private static void addSpecialization(Pool<Integer> ret, SR6SkillValue sVal, String...special) {
		SkillSpecializationValue<SR6Skill> bestSpec = null;
		for (SkillSpecializationValue<SR6Skill> spec : sVal.getSpecializations()) {
			// Test if specializ. matches requested specs
			if (!Arrays.asList(special).contains(spec.getResolved().getId()))
				continue;
			//if (bestSpec == null || spec.isExpertise())
				bestSpec = spec;
		}
		if (bestSpec != null && !sVal.getSkill().getId().contains("exotic")) {
			if (bestSpec.getDistributed()==2) {
				ret.addStep(ValueType.NATURAL, new PoolCalculation<Integer>(3,
						RES.format("explain.expertise", bestSpec.getResolved().getName())));
			} else {
				ret.addStep(ValueType.NATURAL, new PoolCalculation<Integer>(2,
						RES.format("explain.specialization", bestSpec.getResolved().getName())));
			}
		}

	}

//	//--------------------------------------------------------------------
//	public static Pool<Integer> getSkillPool(Shadowrun6Character model, SR6Skill skill, String... special) {
//		return getSkillPool(model, skill, skill.getAttribute(), special);
//	}

	//-------------------------------------------------------------------
	@SuppressWarnings("incomplete-switch")
	public static String getInitiativeString(Shadowrun6Character model, ShadowrunAttribute iniAttribute) {
		String base = model.getAttribute(iniAttribute).toString();
		switch (iniAttribute) {
		case INITIATIVE_PHYSICAL:
			return RES.format("label.ini", base, model.getAttribute(ShadowrunAttribute.INITIATIVE_DICE_PHYSICAL).getModifiedValue());
		case INITIATIVE_MATRIX:
			return RES.format("label.ini", base, model.getAttribute(ShadowrunAttribute.INITIATIVE_DICE_MATRIX).getModifiedValue());
		case INITIATIVE_MATRIX_VR_COLD:
			return RES.format("label.ini", base, model.getAttribute(ShadowrunAttribute.INITIATIVE_DICE_MATRIX_VR_COLD).getModifiedValue());
		case INITIATIVE_MATRIX_VR_HOT:
			return RES.format("label.ini", base, model.getAttribute(ShadowrunAttribute.INITIATIVE_DICE_MATRIX_VR_HOT).getModifiedValue());
		case INITIATIVE_ASTRAL:
			return RES.format("label.ini", base, model.getAttribute(ShadowrunAttribute.INITIATIVE_DICE_ASTRAL).getModifiedValue());
		}
		return ""+base;
	}

	//-------------------------------------------------------------------
	public static String getDrainString(SR6Spell spell) {
		if (spell.getDrain()<0)
			return RES.getString("label.drain.short")+" "+spell.getDrain();
		if (spell.getDrain()>0)
			return RES.getString("label.drain.short")+" +"+spell.getDrain();
		return RES.getString("label.drain.short");
	}

	//-------------------------------------------------------------------
	public static int[] getMonitorArray(Shadowrun6Character model, ShadowrunAttribute attr) {
		int add = 0;
		if (attr==ShadowrunAttribute.BODY && model.getAttribute(ShadowrunAttribute.PHYSICAL_MONITOR)!=null)
			add=model.getAttribute(ShadowrunAttribute.PHYSICAL_MONITOR).getModifiedValue();
		if (attr==ShadowrunAttribute.WILLPOWER && model.getAttribute(ShadowrunAttribute.STUN_MONITOR)!=null)
			add=model.getAttribute(ShadowrunAttribute.STUN_MONITOR).getModifiedValue();
		int[] ret = new int[add];

		int start = 0;
		int every = 3;
		if (model.hasAdeptPower("pain_resistance")) {
			start+=model.getAdeptPower("pain_resistance").getModifiedValue();
		}

		for (int i=start; i<ret.length; i++) {
			ret[i] = - ((i+1-start)/every);
			if (attr==ShadowrunAttribute.BODY && model.hasRuleFlag(SR6RuleFlag.PAIN_TOLERANCE_LOWER_MOD)) {
				if (ret[i]<0)
					ret[i]++;
			}
			if (attr==ShadowrunAttribute.BODY && model.hasRuleFlag(SR6RuleFlag.PAIN_TOLERANCE_DOUBLE_MOD)) {
				ret[i]*=2;
			}
		}
		// Ensure that even for rows smaller than 3 the last box means a higher malus
		if ((add % 3)!=0) {
			ret[add-1] = ret[add-1]-1;
		}

		logger.log(Level.DEBUG, "array for "+attr+": "+Arrays.toString(ret));

		return ret;
	}

	//--------------------------------------------------------------------
	/**
	 * Prepare a single section from a multicolumn table with sections
	 * @param <T> Data type
	 * @param <C> Column type.
	 * @param <S> Section type. Should implement comparable
	 * @param data Data to sort
	 * @param section Section to return
	 * @param detectColumn Method to detect column
	 * @param detectSection Method to detect section
	 * @return
	 */
	public static <T,C,S> Map<C, List<T>> sortToColumns(List<T> data, S section, Function<T,C> detectColumn, Function<T,S> detectSection) {
		List<T> allSorted = new ArrayList<>(data);
		// Sort by sections
		Collections.sort(allSorted, new Comparator<T>() {
			@SuppressWarnings("unchecked")
			public int compare(T o1, T o2) {
				S section1 = detectSection.apply(o1);
				S section2 = detectSection.apply(o2);
				if (section1==null && section2==null) return 0;
				if (section1==null && section2!=null) return +1;
				if (section1!=null && section2==null) return -1;
				if (section instanceof Comparable) {
					return ((Comparable<S>)section1).compareTo(section2);
				} else
					return String.valueOf(section1).compareTo(String.valueOf(section2));
			}
		});

		Map<C, List<T>> ret = new HashMap<>();
		for (T item : data) {
			// Ignore data from unwanted section
			if (section!=detectSection.apply(item))
				continue;
			// Sort to matching column
			C column = detectColumn.apply(item);
			List<T> list = ret.get(column);
			if (list==null) {
				list = new ArrayList<>();
				ret.put(column, list);
			}
			list.add(item);
		}

		return ret;
	}

	//--------------------------------------------------------------------
	/**
	 * @param <T>
	 * @param <C>
	 * @param data
	 * @param columns
	 * @param minRows
	 * @param detectCategory
	 * @param categoryCompare
	 * @return
	 */
	public static <T,C> List<Object>[] getAsBalancedCategoryTable(List<T> data, int columns, int minRows, Function<T, C> detectCategory, Comparator<C> categoryCompare) {
		Map<C, List<Object>> listsByCategory = new HashMap<>();
		// Sort all data into categorized lists
		for (T item : data) {
			C category = detectCategory.apply(item);
			List<Object> list = listsByCategory.get(category);
			if (list==null) {
				list = new ArrayList<>();
				list.add(category); // Add header to list
				listsByCategory.put(category, list);
			}
			list.add(item);
		}

		// Make a first guess for required rows
		int totalItems = data.size()+listsByCategory.size();
		int rowsFirstAssumption = totalItems/columns;
		if ((totalItems%columns)>0)
			rowsFirstAssumption++;
		int guessedRows = Math.max(minRows, rowsFirstAssumption);

		List<C> categories = new ArrayList<>(listsByCategory.keySet());
		Collections.sort(categories, categoryCompare);
		@SuppressWarnings("unchecked")
		Class<C> categoryClass = (Class<C>) categories.get(0).getClass();

		// Prepare result
		@SuppressWarnings("unchecked")
		List<Object>[] resultLists = new ArrayList[columns];
		for (int i=0; i<columns; i++)
			resultLists[i] = new ArrayList<>();

		/*
		 * Try several iterations
		 */
		int rows = guessedRows;
		outer:
		while ( (rows-guessedRows)<4 ) {
			int maxItems = rows*columns;
			// Build a combined list of all categories
			List<Object> all = new ArrayList<Object>();
			categories.forEach(cat -> all.addAll(listsByCategory.get(cat)));

			/*
			 * Ensure that there is no category at a column end.
			 * If so, fill an empty line there
			 */
			for (int col=0; col<columns; col++) {
				int colEnd = ((col+1)*rows)-1;
				Object item = (colEnd<all.size())?all.get(colEnd):null;
				if (item!=null && categoryClass.isInstance(item)) {
					// Last element in column was a category header
					// Inject an empty line here
					all.add(colEnd, null);
				}
			}
			// If after all eventually injects the number of items does
			// not exceed maximum, we are okay
			if (all.size()<=maxItems) {
				// Fill into result lists
				for (int i=0; i<columns; i++) {
					int to = Math.min( ((i+1)*rows), all.size());
					if (i*rows <= to)
						resultLists[i].addAll(all.subList(i*rows, to));
				}
				break outer;
			}
			// Otherwise try with a row more
			rows++;
		}

		return resultLists;
	}

	//--------------------------------------------------------------------
	/**
	 * @param <T>
	 * @param <C>
	 * @param data
	 * @param columns
	 * @param minRows
	 * @param detectCategory
	 * @param categoryCompare
	 * @return
	 */
	public static <T,C> List<Object> getAsBalancedCategoryList(List<T> data, int columns, int minRows, Function<T, C> detectCategory, Comparator<C> categoryCompare) {
		List<Object>[] raw = getAsBalancedCategoryTable(data, columns, minRows, detectCategory, categoryCompare);
		List<Object> ret = new ArrayList<>();
		while (true) {
			for (int i=0; i<columns; i++) {
				if (raw[i].isEmpty()) {
					if (i==0)
						return ret;
					else
						ret.add(null);
				} else {
					ret.add(raw[i].remove(0));
				}
			}
		}
	}
	//-------------------------------------------------------------------
	public static List<SR6SkillValue> getAllSkillValues(Shadowrun6Character model, SkillType... types) {
		List<SkillType> filter = Arrays.asList(types);
		if (filter.isEmpty())
			filter = Arrays.asList(SkillType.regularValues());

		List<SR6SkillValue> ret = new ArrayList<>();
		for (SR6Skill skill : Shadowrun6Core.getSkills()) {
			if (!filter.contains(skill.getType()))
				continue;
			switch (skill.getType()) {
			case COMBAT:
			case MAGIC:
			case PHYSICAL:
			case RESONANCE:
			case SOCIAL:
			case TECHNICAL:
			case VEHICLE:
				SR6SkillValue val = model.getSkillValue(skill);
				if (val==null) {
					if (skill.isUseUntrained()) {
						val = new SR6SkillValue(skill, 0); // -1
					} else
						val = new SR6SkillValue(skill, -1); // -99
				}
				ret.add(val);
				break;
			case LANGUAGE:
			case KNOWLEDGE:
				break;
			case ACTION:
			case NOT_SET:
				break;
			}
		}

		for (SR6SkillValue val : model.getSkillValues()) {
			SkillType tmpType = val.getModifyable().getType();
			if (!filter.contains(tmpType))
				continue;
			if (Arrays.asList(SkillType.individualValues()).contains(tmpType)) {
				ret.add(val);
			}
		}

		return ret;
	}

	//-------------------------------------------------------------------
	public static Pool<Integer> getWeaponPoolCalculation(Shadowrun6Character model, CarriedItem<ItemTemplate> item) {
		Pool<Integer> pool = new Pool<Integer>();
		
		if (item.getAsObject(SR6ItemAttribute.POOL_SUPPLIER)!=null) {
			return ((BiFunction<Shadowrun6Character,CarriedItem<ItemTemplate>,Pool<Integer>>) item.getAsObject(SR6ItemAttribute.POOL_SUPPLIER).getModifiedValue())
					.apply(model, item);
		}

		if (item.getAsObject(SR6ItemAttribute.SKILL)==null) {
			logger.log(Level.ERROR, "No SKILL attribute for weapon {0}", item.getNameWithoutRating());
			return new Pool<Integer>();
		}

		SR6Skill skill = (SR6Skill) item.getAsObject(SR6ItemAttribute.SKILL).getValue();
		ItemAttributeObjectValue<SR6ItemAttribute> aSpec = item.getAsObject(SR6ItemAttribute.SKILL_SPECIALIZATION);
		if (aSpec==null) {
			logger.log(Level.ERROR, "No SKILL_SPECIALIZATION attribute for weapon {0}", item.getNameWithoutRating());
		}
		SkillSpecialization<SR6Skill> spec = (aSpec!=null)?(SkillSpecialization<SR6Skill>) aSpec.getValue():null;
		if (spec!=null)
			pool = getSkillPool(model, skill, spec.getId());
		else
			pool = getSkillPool(model, skill);

		/*
		 * Add eventually existing focus
		 */
		logger.log(Level.DEBUG, "Shadowrun6Tool.getWeaponPoolCalculation: ToDo: Check for weapon focus");
//		if (item.getUsedFocus()!=null) {
//			FocusValue focus = item.getUsedFocus();
//			if (focus.getModifyable().getChoice()==ChoiceType.MELEE_WEAPON) {
//				ret.add( new PoolCalculation(focus.getLevel(), focus.getName()));
//			}
//		}

		/*
		 * Add eventually existing item attunement
		 */
		//System.err.println("Shadowrun6Tool.getWeaponPoolCalculation: Modification of "+item+" are "+item.getCharacterModifications());
		for (Modification mod : item.getOutgoingModifications()) {
			if (mod instanceof CheckModification) {
				CheckModification cMod = (CheckModification)mod;
				if (cMod.getWhat()==ShadowrunCheckInfluence.DICE) {
					String name = ((DataItem)cMod.getSource()).getName();
					pool.addStep(ValueType.NATURAL, new PoolCalculation<Integer>(cMod.getValue(),name, true));
				} else {
					pool.addCheckModification(cMod);
				}
			}
		}
		logger.log(Level.DEBUG, "Pool of {0} is {1}", item.getNameWithoutRating(), pool.toString());

		return pool;
	}

	//--------------------------------------------------------------------
	public static Pool<Integer> getWeaponPool(Shadowrun6Character model, CarriedItem<ItemTemplate> item) {
		return getWeaponPoolCalculation(model, item);
	}

	//--------------------------------------------------------------------
	public static String getWeaponPoolExplanation(Shadowrun6Character model, CarriedItem<ItemTemplate> item) {
		return getWeaponPoolCalculation(model, item).toExplainString();
	}

	//---------------------------------------------------------
	public static List<WeaponValues> getWeaponModes(Shadowrun6Character model, CarriedItem<ItemTemplate> item) {
		List<WeaponValues> ret = new ArrayList<>();

		ItemType type = (ItemType) item.getAsObject(SR6ItemAttribute.ITEMTYPE).getModifiedValue();
		if (ItemType.isWeapon(type)) {
			Damage damage = item.getAsObject(SR6ItemAttribute.DAMAGE).getModifiedValue();
			WeaponDamageConverter conv = new WeaponDamageConverter();
			int[] rawAR = (int[]) item.getAsObject(SR6ItemAttribute.ATTACK_RATING).getModifiedValue();

			WeaponValues ss = new WeaponValues();
			ss.name = FireMode.SINGLE_SHOT.getName(Locale.getDefault());
			ss.attackRating = rawAR;
			ss.pool = getWeaponPool(model, item).getValue(ValueType.NATURAL);
			try { ss.dmg  = conv.write(damage); } catch (Exception e) {
				logger.log(Level.ERROR,"Failed converting weapon damage",e);
			}
			logger.log(Level.WARNING, ss.toString());
			ret.add(ss);

			List<FireMode> modes = (item.hasAttribute(SR6ItemAttribute.FIREMODES))?item.getAsObject(SR6ItemAttribute.FIREMODES).getModifiedValue():List.of();
			if (modes!=null) {
				// Semi-Automatic (CRB 109)
				if (modes.contains(FireMode.SEMI_AUTOMATIC)) {
					WeaponValues sa = new WeaponValues();
					sa.name = FireMode.SEMI_AUTOMATIC.getName(Locale.getDefault());
					sa.attackRating = new int[5];
					for (int i=0; i<5; i++) {
						switch (rawAR[i]) {
						case 0: case 1:
							sa.attackRating[i]=0; break;
						default:
							sa.attackRating[i]= rawAR[i]-2;
						}
					}
					sa.pool = getWeaponPool(model, item).getValue(ValueType.NATURAL);
					Damage tmp = new Damage(
							damage.getValue() +1,
							damage.getType(),
							damage.getElement()
							);
					try { sa.dmg  = conv.write(tmp); } catch (Exception e) {
						logger.log(Level.ERROR,"Failed converting weapon damage",e);
					}
					logger.log(Level.WARNING,sa.toString());
					ret.add(sa);
				}
				// Burst Fire (Wide and Narrow)
				// Semi-Automatic (CRB 109)
				if (modes.contains(FireMode.BURST_FIRE)) {
					// Wide
					WeaponValues sa = new WeaponValues();
					sa.name = FireMode.BURST_FIRE.getName(Locale.getDefault())+" "+Shadowrun6Core.getI18nResources().getString("label.burstfire.wide");
					sa.attackRating = new int[5];
					for (int i=0; i<5; i++) {
						switch (rawAR[i]) {
						case 0: case 1:
							sa.attackRating[i]=0; break;
						default:
							sa.attackRating[i]= rawAR[i]-2;
						}
					}
					sa.pool = getWeaponPool(model, item).getValue(ValueType.NATURAL) /2;
					Damage tmp = new Damage(
							damage.getValue() +1,
							damage.getType(),
							damage.getElement()
							);
					try { sa.dmg  = conv.write(tmp); } catch (Exception e) {
						logger.log(Level.ERROR,"Failed converting weapon damage",e);
					}
					ret.add(sa);
					// Narrow
					sa = new WeaponValues();
					sa.name = FireMode.BURST_FIRE.getName(Locale.getDefault())+" "+Shadowrun6Core.getI18nResources().getString("label.burstfire.narrow");
					sa.attackRating = new int[5];
					for (int i=0; i<5; i++) {
						switch (rawAR[i]) {
						case 0: case 1: case 2: case 3:
							sa.attackRating[i]=0; break;
						default:
							sa.attackRating[i]= rawAR[i]-4;
						}
					}
					sa.pool = getWeaponPool(model, item).getValue(ValueType.NATURAL);
					tmp = new Damage(
							damage.getValue() +2,
							damage.getType(),
							damage.getElement()
							);
					try { sa.dmg  = conv.write(tmp); } catch (Exception e) {
						logger.log(Level.ERROR,"Failed converting weapon damage",e);
					}
					ret.add(sa);
				}
				// Full Auto(CRB 109)
				if (modes.contains(FireMode.FULL_AUTO)) {
					WeaponValues sa = new WeaponValues();
					sa.name = FireMode.FULL_AUTO.getName(Locale.getDefault());
					sa.attackRating = new int[5];
					for (int i=0; i<5; i++) {
						switch (rawAR[i]) {
						case 0: case 1: case 2: case 3: case 4: case 5:
							sa.attackRating[i]=0; break;
						default:
							sa.attackRating[i]= rawAR[i]-6;
						}
					}
					sa.pool = getWeaponPool(model, item).getValue(ValueType.NATURAL);
					Damage tmp = damage;
					try { sa.dmg  = conv.write(tmp); } catch (Exception e) {
						logger.log(Level.ERROR,"Failed converting weapon damage",e);
					}
					logger.log(Level.WARNING, sa.toString());
					ret.add(sa);
				}
				// RB firing mode (deadly arts 152) in base variant does not change DV or AR, thus needs no extra calculations shown
			} // FireModes!=null
		}

		return ret;
	}

	//---------------------------------------------------------
	public static List<WeaponValues> getConditionalWeaponValues(ShadowrunCharacter model, CarriedItem<ItemTemplate> item) {
		List<WeaponValues> ret = new ArrayList<>();
		ItemType type = (ItemType) item.getAsObject(SR6ItemAttribute.ITEMTYPE).getModifiedValue();
		if (!ItemType.isWeapon(type))
			return ret;

		// Start with base value
		if (item.getResolved().getAttribute(SR6ItemAttribute.ATTACK_RATING)!=null)
			ret.add(new WeaponValues(item.getResolved().getName(), item.getAsObject(SR6ItemAttribute.ATTACK_RATING).getModifiedValue()));

		// Now use all non-conditional modifications to the attack rating
		ItemAttributeObjectValue foo = item.getAsObject(SR6ItemAttribute.ATTACK_RATING);
		for (Modification tmp : foo.getIncomingModifications()) {
			ValueModification mod = (ValueModification)tmp;
			String[] raw = mod.getValueAsKeys();
			int[] ar = (raw.length<=1)?new int[1]:new int[5];
			for (int i=0; i<raw.length; i++) {
				ar[i] = 0;
				if (!raw[i].isBlank()) {
					ar[i] = Integer.parseInt(raw[i]);
				}
			}

			String name = "Unknown source";
			if (mod.getSource()!=null) {
				if (mod.getSource() instanceof DataItem) {
					name = ((DataItem)mod.getSource()).getName();
				} else if (mod.getSource() instanceof ShadowrunAttribute) {
					name = ((ShadowrunAttribute)mod.getSource()).getName();
				}
			}

			if (mod.isConditional()) {
				logger.log(Level.WARNING,"TODO: Ignore conditional "+mod+" for later: "+mod.getConditionString());
				ret.add(new WeaponValues(name, ar));
			} else {
				ret.add(new WeaponValues(name, ar));
			}
		}


		/*
		 * Memorize current settings and than clear all condidtions
		 */
//		List<HasOptionalCondition> memorizedConditions = new ArrayList<>();
//
//		for (HasOptionalCondition cond : item.getAvailableConditions()) {
//			logger.warn("Memorize = "+cond);
//			if (item.assumesCondition(cond)) {
//				memorizedConditions.add(cond);
//				item.removeCondition(cond);
//			}
//		}


			WeaponDamageConverter conv = new WeaponDamageConverter();
//			for (HasOptionalCondition cond : item.getAvailableConditions()) {
////				item.addCondition(cond);
//				logger.info("Arg ="+cond);
//				WeaponValues val = new WeaponValues();
//				val.name = item.getItem().getConditionName(cond);
//				val.pool = getWeaponPool(model, item);
//				val.inactive = !item.assumesCondition(cond);
//
//				if (cond instanceof ItemAttributeModification) {
//					ItemAttributeModification mod = (ItemAttributeModification) cond;
//					int[] ar = (int[]) mod.getObjectValue();
//					if (ar == null)
//						ar = new int[5];
//					if (mod.getValue() != 0) {
//						for (int i = 0; i < ar.length; i++)
//							ar[i] = mod.getValue();
//					}
//					val.attackRating = ar;
//				} else {
//					val.attackRating = (int[]) item.getAsObject(ItemAttribute.ATTACK_RATING).getModifiedValue();
//				}
//				try {
//					Damage tmp = new Damage(
//							item.getAsValue(ItemAttribute.DAMAGE).getModifiedValue(),
//							false,
//							item.getItem().getWeaponData().getDamage().getType(),
//							item.getItem().getWeaponData().getDamage().getWeaponDamageType()
//							);
//					val.dmg  = conv.write(tmp);
//				} catch (Exception e) {
//					logger.error("Failed converting weapon damage",e);
//				}
//				ret.add(val);
//
////				item.removeCondition(cond);
//			}

		// Reapply memorized conditions
//		for (HasOptionalCondition memo : memorizedConditions) {
//			item.addCondition(memo);
//		}

		return ret;
	}


	//-------------------------------------------------------------------
	/*
	 * Called from Shadowrun6_Print
	 */
	public static Damage getWeaponDamage(Shadowrun6Character model, CarriedItem<ItemTemplate> item) {
		if (!item.hasAttribute(SR6ItemAttribute.DAMAGE)) {
			logger.log(Level.ERROR, "No DAMAGE attribute for weapon {0}", item.getNameWithoutRating());
			return new Damage();
		}
		Damage damage = (Damage)item.getAsObject(SR6ItemAttribute.DAMAGE).getModifiedValue();
		return damage;
	}

	//-------------------------------------------------------------------
	public static String getItemAttributeString(Shadowrun6Character model, CarriedItem<ItemTemplate> item, SR6ItemAttribute attr) {
		if (!item.hasAttribute(attr)) return "-";
		switch (attr) {
		case FIREMODES:
			return String.join("," , ((List<FireMode>)item.getAsObject(SR6ItemAttribute.FIREMODES).getValue()).stream().map(fm -> fm.getName(Locale.getDefault())).toList() );
		case SKILL:
			return ((SR6Skill)item.getAsObject(attr).getValue()).getName();
		case PRICE:
			return String.valueOf(item.getAsObject(attr));
		case AMMUNITION:
			return String.join(", ", ((List<AmmunitionSlot>)item.getAsObject(attr).getModifiedValue()).stream().map(a -> a.toString()).toList());
		default:
			ItemAttributeNumericalValue val = item.getAsValue(attr);
			if (val.getModifier()==0)
				return String.valueOf(val.getDistributed());
			else
				return val.getDistributed()+" ("+val.getModifiedValue()+")";
		}
	}

	//--------------------------------------------------------------------
	/*
	 * Find the best commlink or cyber jack
	 */
	public static CarriedItem<ItemTemplate> getPrimaryMatrixDF(Shadowrun6Character model) {
		// Check if there is a device which is flagged PRIMARY
		Optional<CarriedItem<ItemTemplate>> opt = model.getCarriedItemsRecursive().stream()
				.filter(i -> i.hasFlag(SR6ItemFlag.MATRIX_DEVICE)).filter(i -> i.hasFlag(SR6ItemFlag.PRIMARY))
				.filter(i -> i.hasAttribute(SR6ItemAttribute.DATA_PROCESSING)).findFirst();
		if (opt.isPresent()) {
			return opt.get();
		} else {
			CarriedItem<ItemTemplate> best = null;
			int bestSum = 0;
			for (CarriedItem<ItemTemplate> item : model.getCarriedItemsRecursive()) {
				if (!item.hasAttribute(SR6ItemAttribute.DATA_PROCESSING))
					continue;
				item.removeFlag(SR6ItemFlag.PRIMARY);
				int a = item.getAsValue(SR6ItemAttribute.DATA_PROCESSING).getModifiedValue();
				int s = item.getAsValue(SR6ItemAttribute.FIREWALL).getModifiedValue();
				int sum = a + s;
				if (sum > bestSum) {
					// Previous best is not best anymore
					best = item;
					bestSum = sum;
				}
			}
			if (best!=null)
				best.addFlag(SR6ItemFlag.PRIMARY);

			return best;
		}
	}

	//--------------------------------------------------------------------
	/*
	 * Find the best commlink or cyber jack
	 */
	public static CarriedItem<ItemTemplate> getPrimaryMatrixAS(Shadowrun6Character model) {
		// Check if there is a device which is flagged PRIMARY
		Optional<CarriedItem<ItemTemplate>> opt = model.getCarriedItemsRecursive().stream()
				.filter(i -> i.hasFlag(SR6ItemFlag.MATRIX_DEVICE)).filter(i -> i.hasFlag(SR6ItemFlag.PRIMARY))
				.filter(i -> i.hasAttribute(SR6ItemAttribute.ATTACK)).findFirst();
		if (opt.isPresent()) {
			return opt.get();
		} else {
			CarriedItem<ItemTemplate> bestAS = null;
			int bestSum = 0;
			for (CarriedItem<ItemTemplate> item : model.getCarriedItemsRecursive()) {
				if (!item.hasAttribute(SR6ItemAttribute.ATTACK))
					continue;
				item.removeFlag(SR6ItemFlag.PRIMARY);
				int a = item.getAsValue(SR6ItemAttribute.ATTACK).getModifiedValue();
				int s = item.getAsValue(SR6ItemAttribute.SLEAZE).getModifiedValue();
				int sum = a + s;
				if (sum > bestSum) {
					// Previous best is not best anymore
					bestAS = item;
					bestSum = sum;
				}
			}
			if (bestAS!=null)
				bestAS.addFlag(SR6ItemFlag.PRIMARY);

			return bestAS;
		}
	}

	//--------------------------------------------------------------------
	/**
	 * Determine the most powerful RCC available
	 */
	public static CarriedItem<ItemTemplate> getBestRCC(Shadowrun6Character model) {
		CarriedItem<ItemTemplate> ret = null;
		//look for carried and  embedded RCCs
		for (CarriedItem<ItemTemplate> item : model.getCarriedItemsRecursive()) {
			// Only evaluate RIGGER_CONSOLEs
			if (getItemSubType(item)!=ItemSubType.RIGGER_CONSOLE)
				continue;
			// If the RCC is the "Living Command Console", derive matrix attributes from character
			if ("living_console".equals(item.getKey())) {
				// Device rating equals to resonance attribute
				int rating = model.getAttribute(ShadowrunAttribute.RESONANCE).getModifiedValue();
				item.setAttribute(SR6ItemAttribute.DEVICE_RATING, new ItemAttributeNumericalValue<SR6ItemAttribute>(SR6ItemAttribute.DEVICE_RATING, rating));
				item.setAttribute(SR6ItemAttribute.DATA_PROCESSING, new ItemAttributeNumericalValue<SR6ItemAttribute>(SR6ItemAttribute.DATA_PROCESSING, model.getAttribute(ShadowrunAttribute.LOGIC).getModifiedValue()));
				item.setAttribute(SR6ItemAttribute.FIREWALL, new ItemAttributeNumericalValue<SR6ItemAttribute>(SR6ItemAttribute.FIREWALL, model.getAttribute(ShadowrunAttribute.WILLPOWER).getModifiedValue()));
			}
			if ("machinist_console".equals(item.getKey())) {
				item.setAttribute(SR6ItemAttribute.DEVICE_RATING, new ItemAttributeNumericalValue<SR6ItemAttribute>(SR6ItemAttribute.DEVICE_RATING, model.getAttribute(ShadowrunAttribute.RESONANCE).getModifiedValue()));
				item.setAttribute(SR6ItemAttribute.DATA_PROCESSING, new ItemAttributeNumericalValue<SR6ItemAttribute>(SR6ItemAttribute.DATA_PROCESSING, model.getAttribute(ShadowrunAttribute.LOGIC).getModifiedValue()));
				item.setAttribute(SR6ItemAttribute.FIREWALL, new ItemAttributeNumericalValue<SR6ItemAttribute>(SR6ItemAttribute.FIREWALL, model.getAttribute(ShadowrunAttribute.WILLPOWER).getModifiedValue()));
				item.setAttribute(SR6ItemAttribute.CONCURRENT_PROGRAMS, new ItemAttributeNumericalValue<SR6ItemAttribute>(SR6ItemAttribute.CONCURRENT_PROGRAMS, model.getAttribute(ShadowrunAttribute.CHARISMA).getModifiedValue()));
				item.setAttribute(SR6ItemAttribute.NOISE_REDUCTION, new ItemAttributeNumericalValue<SR6ItemAttribute>(SR6ItemAttribute.NOISE_REDUCTION, model.getAttribute(ShadowrunAttribute.WILLPOWER).getModifiedValue()));
			}

			if (ret==null 
					|| !ret.hasAttribute(SR6ItemAttribute.DEVICE_RATING)  // No device rating assumes technomancer ability
					|| ret.getAsValue(SR6ItemAttribute.DEVICE_RATING).getModifiedValue()<item.getAsValue(SR6ItemAttribute.DEVICE_RATING).getModifiedValue())
				ret = item;
		}
		
		
		return ret;
	}

	//-------------------------------------------------------------------
	/**
	 * Get a list of all ammunitions from the inventory, that can be used
	 * with a given weapon.
	 *
	 * @param model  Character
	 * @param weapon Weapon
	 * @return
	 */
	public static List<CarriedItem<ItemTemplate>> getAmmunitionsFor(Shadowrun6Character model, CarriedItem<ItemTemplate> weapon) {
		List<CarriedItem<ItemTemplate>> ret = new ArrayList<>();
		boolean caseless = weapon.hasFlag(SR6ItemFlag.USES_CASELESS);
		for (CarriedItem<ItemTemplate> ammo : model.getCarriedItems(ItemUtil.AMMUNITION_FILTER)) {
			boolean ammoIsCaseless = ammo.getVariantID()!=null && ammo.getVariantID().equals("caseless");
			// Determine if ammunition is allowed for this weapon
			for (Requirement req : ammo.getRequirements()) {
				boolean isMet = ItemUtil.isRequirementMet(weapon, ammo.getResolved(), req);
				logger.log(Level.INFO, "Ammo "+ammo+" mets requirement = "+isMet);
				if (isMet) {
					/** check for cased vs. caseless deactivated, as weapons cannot be selected to be caseless
					// Check cased vs. caseless
					if (caseless && ammoIsCaseless)
						ret.add(ammo);
					else if (!caseless && !ammoIsCaseless)
						ret.add(ammo);
					*/
					ret.add(ammo); //delete this line if check for cased vs. caseless is reactivated
				}
			}
		}

		return ret;
	}

	//-------------------------------------------------------------------
	/**
	 *
	 * @param model
	 * @param weapon
	 * @return
	 */
	public static TreeMap<AmmunitionType, Map<SR6ItemAttribute, ItemAttributeValue<SR6ItemAttribute>>> getAmmunitionTypes(Shadowrun6Character model, CarriedItem<ItemTemplate> weapon) {
		TreeMap<AmmunitionType, Map<SR6ItemAttribute, ItemAttributeValue<SR6ItemAttribute>>> ret = new TreeMap<>();
		for (CarriedItem<ItemTemplate> ammo : model.getCarriedItems(ItemUtil.AMMUNITION_FILTER)) {
			// Determine if ammunition is allowed for this weapon
		}

		return ret;
	}

	//-------------------------------------------------------------------
	public static ItemType getItemType(CarriedItem<ItemTemplate> model) {
		if (!model.hasAttribute(SR6ItemAttribute.ITEMTYPE)) {
			if ("unarmed".equals(model.getKey()))
				return ItemType.WEAPON_CLOSE_COMBAT;
			if ("software_library".equals(model.getKey()))
				return ItemType.SOFTWARE;
			logger.log(Level.WARNING, "No ITEMTYPE for "+model.getKey());
			System.err.println("Shadowrun6Tools.getItemType(): No ITEMTYPE for "+model.getKey());
			return null;
		}
		try {
			return model.getAsObject(SR6ItemAttribute.ITEMTYPE).getValue();
		} catch (ClassCastException e) {
			logger.log(Level.ERROR, model.dump());
			logger.log(Level.ERROR, "Wrong attribute type for ITEMTYPE {0} in item {1}",model.getAttributeRaw(SR6ItemAttribute.ITEMTYPE), model.getKey());
			logger.log(Level.ERROR, "asObject = "+model.getAsObject(SR6ItemAttribute.ITEMTYPE));
			model.getAsObject(SR6ItemAttribute.ITEMTYPE).getValue();
			throw e;
		}
	}

	//-------------------------------------------------------------------
	public static ItemSubType getItemSubType(CarriedItem<ItemTemplate> model) {
		if (!model.hasAttribute(SR6ItemAttribute.ITEMSUBTYPE)) {
			logger.log(Level.WARNING, "No ITEMSUBTYPE for "+model.getKey());
			return null;
		}
		return model.getAsObject(SR6ItemAttribute.ITEMSUBTYPE).getValue();
	}

	//---------------------------------------------------------
	public static String getAccessoryString(CarriedItem<ItemTemplate> item) {
		Map<ItemTemplate, Counted> map = new LinkedHashMap<>();
		List<String> list = new ArrayList<>();
		item.getEffectiveAccessories().forEach( ci -> {
			ItemSubType sub = ItemSubType.ACCESSORY;
			if (ci!=null &&  getItemSubType(ci)!=null)
				sub = getItemSubType(ci);
			else {
				if (getItemType(ci)!=null && getItemSubType(ci)!=null) {
					sub = getItemSubType(ci);
				} else
					logger.log(Level.WARNING, "No subtype set for "+ci+" / "+getItemType(ci)+" / "+getItemSubType(ci));
			}
				// Sum up
				if (map.containsKey(ci.getResolved())) {
					map.get(ci.getResolved()).count++;
				} else {
					map.put(ci.getResolved(), new Counted(ci));
				}
		});
		map.values().forEach(c-> list.add(c.toString()));

		String mods = String.join(", ", list);
		return mods;
	}

//	//---------------------------------------------------------
//	public static String getEnhancementString(CarriedItem item) {
//		List<String> list = new ArrayList<>();
//		item.getEnhancements().forEach( mod -> {
//			ItemEnhancement enh = mod.getModifyable();
//			list.add(enh.getName());
//		});
//
//		String mods = String.join(", ", list);
//		return mods;
//	}

	// -------------------------------------------------------------------
	public static List<DataItem> getInfluences(Modifyable val) {
		List<DataItem> ret = new ArrayList<>();
		for (Modification mod : val.getIncomingModifications()) {
			if (mod.isConditional() || mod instanceof CheckModification) {
				if (mod.getSource() == null) {
					System.err.println("Shadowrun6Tools.getInfluences: No source for Modification " + mod);
				} else if (!(mod.getSource() instanceof DataItem)) {
					System.err.println("Shadowrun6Tools.getInfluences: Source of SkillModification " + mod
							+ " is of type " + mod.getSource().getClass());
				} else {
					ret.add((DataItem) mod.getSource());
				}
			}
		}
		return ret;
	}

	//---------------------------------------------------------
	public static CarriedItem<ItemTemplate> flagItemWithHighestAttribute(Shadowrun6Character model, SR6ItemAttribute attrib, SR6ItemFlag flag, boolean set) {
		CarriedItem<ItemTemplate> best = null;
		int highest=Integer.MIN_VALUE;
		for (CarriedItem<ItemTemplate> item : model.getCarriedItems()) {
			if (!item.hasAttribute(attrib))
				continue;

			int val = item.getAsValue(attrib).getModifiedValue();
			// If no previous selection item is better, use it
			if (best==null || val>highest) {
				// Old best remove flag
				if (best!=null) {
					best.setAutoFlag(flag, !set);
					best.setAutoFlag(SR6ItemFlag.IGNORE_FOR_CALCULATIONS, true);
				}
				best = item;
				best.setAutoFlag(flag, set);
				best.setAutoFlag(SR6ItemFlag.IGNORE_FOR_CALCULATIONS, false);
				highest = val;
			} else {
				item.setAutoFlag(SR6ItemFlag.IGNORE_FOR_CALCULATIONS, true);
			}
			logger.log(Level.DEBUG,"*  "+item.getNameWithRating()+" \t"+item.getAsValue(attrib).getModifiedValue()+": ignored="+item.hasAutoFlag(SR6ItemFlag.IGNORE_FOR_CALCULATIONS));
		}
		return best;
	}

	//-----------Get primary armor = has defense_physical and NOT has Cumulative-------------------------------------
	public static CarriedItem<ItemTemplate> getPrimaryArmor(Shadowrun6Character model) {
		// Clear automatic flags
		model.getCarriedItems().stream()
			.filter(ci -> ci.hasAttribute(SR6ItemAttribute.DEFENSE_PHYSICAL) && !ci.hasFlag(SR6ItemFlag.CUMULATIVE))
			.forEach(item -> {
				item.setAutoFlag(SR6ItemFlag.PRIMARY, false);
				item.setAutoFlag(SR6ItemFlag.IGNORE_FOR_CALCULATIONS, true);
			});
		
		// Check if there has been a user selection
		for (CarriedItem<ItemTemplate> item : model.getCarriedItems()) {
			if (!item.hasAttribute(SR6ItemAttribute.DEFENSE_PHYSICAL) || item.hasFlag(SR6ItemFlag.CUMULATIVE))
				continue;
			if (item.hasUserFlag(SR6ItemFlag.PRIMARY)) {
				item.setAutoFlag(SR6ItemFlag.IGNORE_FOR_CALCULATIONS, false);
				item.setAutoFlag(SR6ItemFlag.PRIMARY, true);
				return item;
			}
		}
		
		// No user selection - find best armor
		CarriedItem<ItemTemplate> bestArmor = null;
		for (CarriedItem<ItemTemplate> item : model.getCarriedItems()) {
			if (!item.hasAttribute(SR6ItemAttribute.DEFENSE_PHYSICAL) || item.hasFlag(SR6ItemFlag.CUMULATIVE))
				continue;
			item.setAutoFlag(SR6ItemFlag.PRIMARY, false);
			item.setAutoFlag(SR6ItemFlag.IGNORE_FOR_CALCULATIONS, true);
			// If no previous selection or armor is better, use it
			if (bestArmor==null || item.getAsValue(SR6ItemAttribute.DEFENSE_PHYSICAL).getModifiedValue()> bestArmor.getAsValue(SR6ItemAttribute.DEFENSE_PHYSICAL).getModifiedValue() )
				bestArmor = item;
			logger.log(Level.DEBUG,"*  "+item.getNameWithRating()+" \t"+item.getAsValue(SR6ItemAttribute.DEFENSE_PHYSICAL).getModifiedValue()+": ignored="+item.hasAutoFlag(SR6ItemFlag.IGNORE_FOR_CALCULATIONS));
		}
		if (bestArmor!=null) {
			bestArmor.setAutoFlag(SR6ItemFlag.IGNORE_FOR_CALCULATIONS, false);
			bestArmor.setAutoFlag(SR6ItemFlag.PRIMARY, true);
			return bestArmor;
		}
		return null;
	}

	//-----------Get secondary armor = has defense_physical and has Cumulative and is of a given itemSubType-----------------
	public static CarriedItem<ItemTemplate> getSecondaryArmor(Shadowrun6Character model, ItemSubType subtype) {
		CarriedItem<ItemTemplate> bestArmor = null;
		for (CarriedItem<ItemTemplate> item : model.getCarriedItems()) {
			if (!item.hasAttribute(SR6ItemAttribute.DEFENSE_PHYSICAL) || !item.hasFlag(SR6ItemFlag.CUMULATIVE))
				continue;
			if (!Shadowrun6Tools.isSubtype(item, subtype))
				continue;
			item.setAutoFlag(SR6ItemFlag.PRIMARY, false);
			item.setAutoFlag(SR6ItemFlag.IGNORE_FOR_CALCULATIONS, true);
			// If no previous selection or armor is better, use it
			if (bestArmor==null || item.getAsValue(SR6ItemAttribute.DEFENSE_PHYSICAL).getModifiedValue()> bestArmor.getAsValue(SR6ItemAttribute.DEFENSE_PHYSICAL).getModifiedValue() )
				bestArmor = item;
			logger.log(Level.DEBUG,"*  "+item.getNameWithRating()+" \t"+item.getAsValue(SR6ItemAttribute.DEFENSE_PHYSICAL).getModifiedValue()+": ignored="+item.hasAutoFlag(SR6ItemFlag.IGNORE_FOR_CALCULATIONS));
		}
		if (bestArmor!=null) {
			bestArmor.setAutoFlag(SR6ItemFlag.IGNORE_FOR_CALCULATIONS, false);
			return bestArmor;
		}
		return null;
	}
	
	//---------------------------------------------------------
	public static CarriedItem<ItemTemplate> getPrimaryRangedWeapon(Shadowrun6Character model) {
		List<CarriedItem<ItemTemplate>> weapons = model.getCarriedItems(ItemType.WEAPON_RANGED, ItemType.WEAPON_FIREARMS, ItemType.WEAPON_FIREARMS);
		for (CarriedItem<ItemTemplate> item : weapons) {
			if (item.hasFlag(SR6ItemFlag.PRIMARY)) return item;
		}
		return weapons.isEmpty()?null:weapons.get(0);
	}

	//---------------------------------------------------------
	public static CarriedItem<ItemTemplate> getPrimaryMeleeWeapon(Shadowrun6Character model) {
		List<CarriedItem<ItemTemplate>> weapons = model.getCarriedItems(ItemType.WEAPON_CLOSE_COMBAT);
		for (CarriedItem<ItemTemplate> item : weapons) {
			if (item.hasFlag(SR6ItemFlag.PRIMARY)) return item;
		}
		return weapons.isEmpty()?null:weapons.get(0);
	}

	//---------------------------------------------------------
	public static void applyReward(Shadowrun6Character model, Reward reward) {
		logger.log(Level.INFO, "Apply reward ''{0}'' to character", reward);
		model.addReward(reward);

		if (reward.getExperiencePoints()!=0) {
			model.setKarmaFree( model.getKarmaFree() + reward.getExperiencePoints() );
		}
		if (reward.getMoney()!=0) {
			model.setNuyen( model.getNuyen() + reward.getMoney() );
		}
		ValueModification modHeat = reward.getModification(ShadowrunReference.ATTRIBUTE, ShadowrunAttribute.HEAT.name());
		if (modHeat!=null) {
			AttributeValue<ShadowrunAttribute> val = model.getAttribute(ShadowrunAttribute.HEAT);
			if (val==null) {
				val = new AttributeValue<ShadowrunAttribute>(ShadowrunAttribute.HEAT);
				model.setAttribute(val);
			}
			val.setDistributed( val.getDistributed() + modHeat.getValue());
		}
		ValueModification modRep = reward.getModification(ShadowrunReference.ATTRIBUTE, ShadowrunAttribute.REPUTATION.name());
		if (modRep!=null) {
			AttributeValue<ShadowrunAttribute> val = model.getAttribute(ShadowrunAttribute.REPUTATION);
			if (val==null) {
				val = new AttributeValue<ShadowrunAttribute>(ShadowrunAttribute.REPUTATION);
				model.setAttribute(val);
			}
			val.setDistributed( val.getDistributed() + modHeat.getValue());
		}
	}

	//---------------------------------------------------------
	public static void removeReward(Shadowrun6Character model, Reward reward) {
		logger.log(Level.INFO, "Remove reward ''{0}'' from character", reward.getTitle());
		if (!model.removeReward(reward)) {
			logger.log(Level.ERROR, "Trying to remove a reward that the character doesn't have");
			return;
		}
		model.setKarmaFree( model.getKarmaFree() - reward.getExperiencePoints() );
		model.setNuyen( model.getNuyen() - reward.getMoney() );
		ValueModification modHeat = reward.getModification(ShadowrunReference.ATTRIBUTE, ShadowrunAttribute.HEAT.name());
		if (modHeat!=null) {
			AttributeValue<ShadowrunAttribute> val = model.getAttribute(ShadowrunAttribute.HEAT);
			val.setDistributed( val.getDistributed() - modHeat.getValue());
		}
		ValueModification modRep = reward.getModification(ShadowrunReference.ATTRIBUTE, ShadowrunAttribute.REPUTATION.name());
		if (modRep!=null) {
			AttributeValue<ShadowrunAttribute> val = model.getAttribute(ShadowrunAttribute.REPUTATION);
			val.setDistributed( val.getDistributed() - modHeat.getValue());
		}
	}


	//-------------------------------------------------------------------
	public static <T extends DataItem> Predicate<T> filterByLanguage(Locale loc) {
		return (item) -> item.getLanguage()==null || item.getLanguage().equals(loc.getLanguage());
	}

	//-------------------------------------------------------------------
	@Deprecated
	public static <T extends DataItem> Predicate<T> filterByLanguage(Class<T> cls, Locale loc) {
		return (item) -> item.getLanguage()==null || item.getLanguage().equals(loc.getLanguage());
	}

	//-------------------------------------------------------------------
	public static <T extends DataItem> List<T> filterByPluginSelection(List<T> unfiltered, Shadowrun6Character model) {
//		if (model.getPluginMode()==PluginMode.ALL)
			return unfiltered;
//		List<T> filtered = new ArrayList<T>();
//		for (T tmp : unfiltered) {
//			if (tmp.getPlugin().getID().equals("CORE")) {
//				filtered.add(tmp);
//			} else if (model.getPluginMode()==PluginMode.LANGUAGE || model.getPluginMode()==null) {
//				if (tmp.getPlugin().getLanguages().contains(Locale.getDefault().getLanguage())) {
//					filtered.add(tmp);
//				}
//			} else {
//				// Selected
//				if (model.getPermittedPlugins()!=null && model.getPermittedPlugins().contains(tmp.getPlugin().getID())) {
//					filtered.add(tmp);
//				}
//			}
//
//		}
//		return filtered;
	}

	//-------------------------------------------------------------------
	public static List<CheckModification> getEdgeGenerators(Shadowrun6Character model) {
		List<CheckModification> ret = new ArrayList<>();

		return ret;
	}

	//-------------------------------------------------------------------
	public static int getDefenseRatingUsing(Shadowrun6Character model, CarriedItem<ItemTemplate> primary) {
		int sum = 0;
		AttributeValue<ShadowrunAttribute> aVal = model.getAttribute(ShadowrunAttribute.DEFENSE_RATING_PHYSICAL);
		for (Modification mod : aVal.getIncomingModifications()) {
			if (mod.getSource() instanceof CarriedItem) {
				CarriedItem<ItemTemplate> src = (CarriedItem<ItemTemplate>) mod.getSource();
				if (!src.hasFlag(SR6ItemFlag.PRIMARY)) {
					// Allow helmets
					sum += ((ValueModification)mod).getValue();
				}
			} else {
				sum += ((ValueModification)mod).getValue();
			}
		}

		if (primary.hasAttribute(SR6ItemAttribute.DEFENSE_PHYSICAL)) {
			sum +=primary.getAsValue(SR6ItemAttribute.DEFENSE_PHYSICAL).getModifiedValue();
		}

		return sum;
	}

	//-------------------------------------------------------------------
	public static Map<Object, List<CarriedItem<ItemTemplate>>> getCyberPrograms(Shadowrun6Character model) {
		Map<Object,List<CarriedItem<ItemTemplate>>> ret = new HashMap<>();

		if (model.getMagicOrResonanceType()!=null && model.getMagicOrResonanceType().usesResonance()) {
			// Check complex forms
			List<CarriedItem<ItemTemplate>> list = new ArrayList<>();
			int dataProc = model.getPersona().getDataProcessing().getModifiedValue();
			for (ComplexFormValue val : model.getComplexForms()) {
				if ("emulate".equals(val.getKey())) {
					Decision dec = val.getDecisionByType(ShadowrunReference.PROGRAM);
					if (dec==null) {
						logger.log(Level.ERROR, "No decision what program to emulate in complex form 'emulate'");
						continue;
					}
					ItemTemplate temp = Shadowrun6Core.getItem(ItemTemplate.class, dec.getValue());
					CarriedItem<ItemTemplate> item = new CarriedItem<ItemTemplate>(temp, null, CarryMode.EMBEDDED);
					if (temp.getChoice(ItemTemplate.UUID_RATING)!=null) {
						item.addDecision(new Decision(ItemTemplate.UUID_RATING, String.valueOf(dataProc)));
					}
					SR6GearTool.recalculate("", model, item);
					list.add(item);
				}
			}
			if (!list.isEmpty())
				ret.put(ShadowrunReference.COMPLEX_FORM, list);

			// Build a list of software absorbed by an echo
			list = new ArrayList<>();
			for (CarriedItem<ItemTemplate> item : model.getCarriedItems(ItemType.SOFTWARE)) {
				// Has the software been bought
				if (item.isAutoAdded())
					continue;
				// Has the software already been absorbed
				if (item.hasFlag(SR6ItemFlag.ABSORBED))
					list.add(item);
			}
			if (!list.isEmpty())
				ret.put(ShadowrunReference.METAECHO, list);
		}

		// Installed on hardware
		for (CarriedItem<ItemTemplate> item : model.getCarriedItems(ItemType.ELECTRONICS)) {
			// Has the software been bought
			if (!item.hasFlag(SR6ItemFlag.MATRIX_DEVICE))
				continue;
			List<CarriedItem<ItemTemplate>> list = new ArrayList<>();
			for (CarriedItem<ItemTemplate> item2 : item.getAccessories()) {
				ItemType type2 = item2.getAsObject(SR6ItemAttribute.ITEMTYPE).getModifiedValue();
				if (type2!=ItemType.SOFTWARE)
					continue;
				list.add(item2);
			}
			if (!list.isEmpty())
				ret.put(item, list);
		}


		return ret;
	}

	//-------------------------------------------------------------------
	public static String toExplainString(List<Modification> modifications) {
		if (modifications.isEmpty()) return null;
		List<String> ret = new ArrayList<>();
		for (Modification mod : modifications) {
			Object srcO = mod.getSource();
			String src = String.valueOf(srcO);
			if (srcO instanceof ShadowrunAttribute) {
				src = ((ShadowrunAttribute)srcO).getName();
			} else if (srcO instanceof DataItem) {
				src = ((DataItem)srcO).getName();
			}
			ret.add(  ((ValueModification)mod).getValue()+" "+src);
		}
		return String.join("\n", ret);
	}

	//-------------------------------------------------------------------
	public static String toExplainStringFloat(List<Modification> modifications) {
		if (modifications.isEmpty()) return null;
		List<String> ret = modifications.stream()
				.map(pc -> (((ValueModification)pc).getValueAsDouble()/1000.0)+" "+pc.getSource())
				.collect(Collectors.toList());
		return String.join("\n", ret);
	}

	//-------------------------------------------------------------------
	public static String toExplainStringObject(List<Modification> modifications) {
		if (modifications.isEmpty()) return null;
		List<String> ret = new ArrayList<>();
		for (Modification mod : modifications) {
			Object srcO = mod.getSource();
			String src = String.valueOf(srcO);
			if (srcO instanceof ShadowrunAttribute) {
				src = ((ShadowrunAttribute)srcO).getName();
			} else if (srcO instanceof DataItem) {
				src = ((DataItem)srcO).getName();
			}
			try {
				ret.add(  ((ValueModification)mod).getRawValue()+" "+src);
			} catch (Exception e) {
				logger.log(Level.ERROR, "Error converting "+mod+" to display string",e);
			}
		}
//		List<String> ret = modifications.stream()
//				.map(pc -> ((ValueModification)pc).getRawValue()+" "+pc.getSource())
//				.collect(Collectors.toList());
		return String.join("\n", ret);
	}

	//-------------------------------------------------------------------
	public static <T extends ComplexDataItem, V extends ComplexDataItemValue<T>> V getMatchIncludingDecisions(List<V> haystack, String needleID, List<Decision> decisions) {
		for (V value : haystack) {
			if (!(value.getKey().equals(needleID)))
				continue;
			// ID matches - now compare decisions
			T resolved = value.getResolved();
			if (resolved.getChoices().isEmpty())
				return value;
			for (Choice choice : resolved.getChoices()) {
				Decision haystackDecision = value.getDecision(choice.getUUID());
				Decision needleDecision = decisions.stream().filter(d -> d.getChoiceUUID().equals(choice.getUUID())).findFirst().orElse(null);
				if (haystackDecision!=null && needleDecision!=null && haystackDecision.getValue().equals(needleDecision.getValue())) {
					return value;
				}
			}
		}
		return null;
	}

	//-------------------------------------------------------------------
	private static ValueModification toValueMod(Shadowrun6Character model, ComplexDataItem value) {
		String typeString = value.getTypeString().toUpperCase();
		if (typeString.equals("ITEM")) typeString = "GEAR";
		ShadowrunReference type = ShadowrunReference.valueOf(typeString);
		for (Modification tmp : value.getOutgoingModifications()) {
			if (tmp instanceof ValueModification) {
				ValueModification vmod = (ValueModification)tmp;
				if (vmod.getReferenceType()==ShadowrunReference.ATTRIBUTE && (vmod.getKey().equals("ESSENCE") || vmod.getKey().equals("ESSENCE_HOLE"))) {
					double essence = vmod.getValueAsDouble();
					if (Math.abs(essence)<100)
						essence *= 1000;
					if (vmod.getKey().equals("ESSENCE_HOLE"))
						essence *= -1;
//					// Indicate that essence whole changes should be calculated at runtime
//					if (vmod.getKey().equals("ESSENCE_HOLE")) essence=0;
					ValueModification mod = new ValueModification(type, value.getId(), (int)essence);
					mod.setWhen(null);
					mod.setSet(null);
					mod.setSource(value);
					return mod;
				}
			}
		}
		return null;
	}

	//-------------------------------------------------------------------
	private static ValueModification toValueMod(Shadowrun6Character model, ComplexDataItemValue<?> value) {
		if (value instanceof CarriedItem) {
			CarriedItem<ItemTemplate> item = (CarriedItem<ItemTemplate>) value;
			if (item.hasAttribute(SR6ItemAttribute.ESSENCECOST)) {
				double essence = item.getAsFloat(SR6ItemAttribute.ESSENCECOST).getModifiedValue();
				if (Math.abs(essence)<100)
					essence *= 1000;
				ValueModification mod = new ValueModification(ShadowrunReference.CARRIED, value.getResolved().getId(), (int)(essence));
				mod.setId(value.getUuid());
				mod.setWhen(null);
				mod.setSet(null);
				mod.setSource(value);
				return mod;
			}
		}

		String typeString = value.getResolved().getTypeString().toUpperCase();
		if (typeString.equals("ITEM")) typeString = "GEAR";
		ShadowrunReference type = ShadowrunReference.valueOf(typeString);
		for (Modification tmp : value.getOutgoingModifications()) {
			if (tmp instanceof ValueModification) {
				ValueModification vmod = (ValueModification)tmp;
				if (vmod.getReferenceType()==ShadowrunReference.ATTRIBUTE && (vmod.getKey().equals("ESSENCE") || vmod.getKey().equals("ESSENCE_HOLE"))) {
					double essence = vmod.getValueAsDouble();
					if (Math.abs(essence)<100)
						essence *= 1000;
					if (vmod.getKey().equals("ESSENCE_HOLE"))
						essence *= -1;
					// Indicate that essence whole changes should be calculated at runtime
					if (vmod.getKey().equals("ESSENCE_HOLE")) essence=0;
					ValueModification mod = new ValueModification(type, value.getResolved().getId(), (int)essence);
					mod.setId(value.getUuid());
					mod.setWhen(null);
					mod.setSet(null);
					mod.setSource(value);
					return mod;
				}
			}
		}
		return null;
	}

	//-------------------------------------------------------------------
	public static ValueModification recordEssenceChange(Shadowrun6Character model, ComplexDataItemValue<?> value) {
		ValueModification mod = toValueMod(model, value);
		if (mod!=null) {
			model.getEssenceChanges().add(mod);
			return mod;
		}
		return null;
	}

	//-------------------------------------------------------------------
	public static ValueModification recordEssenceChange(Shadowrun6Character model, ComplexDataItem value) {
		ValueModification mod = toValueMod(model, value);
		if (mod!=null) {
			model.getEssenceChanges().add(mod);
			return mod;
		}
		return null;
	}

	//-------------------------------------------------------------------
	public static void removeEssenceChange(Shadowrun6Character model, ComplexDataItemValue<?> value, RemoveMode mode) {
		logger.log(Level.WARNING, "ENTER removeEssenceChange( {1}, {0} )", mode, value);
		try {
		ValueModification toRemove = null;
		for (ValueModification vMod : model.getEssenceChanges()) {
			if (vMod.getId()!=null && vMod.getId().equals(value.getUuid())) {
				toRemove = vMod;
				break;
			}
		}
		logger.log(Level.WARNING, "remove essence change {0}",toRemove);

		if (toRemove==null)
			return;
		int pos = model.getEssenceChanges().indexOf(toRemove);

		if (mode==RemoveMode.REMOVE_LATE) {
			ValueModification mod = toValueMod(model, value);
			String lblEssHole = RES.format("label.essencehole_for",  value.getNameWithoutDecisions(Locale.getDefault()));
			int EssHole = (-1 * mod.getValue()); // essence hole gets negative value
			mod = new ValueModification(ShadowrunReference.TEXT, lblEssHole, EssHole);
			logger.log(Level.WARNING, "hole mod = "+mod);
			model.getEssenceChanges().add(mod); // essence hole needs to be added to end of list
		} else {
			model.getEssenceChanges().remove(toRemove); // only remove original line if not REMOVE_LATE
		}
		} finally {
			logger.log(Level.WARNING, "LEAVE removeEssenceChange: now {0}", model.getEssenceChanges());
		}
	}

	//-------------------------------------------------------------------
	public static void removeEssenceChange(Shadowrun6Character model, ComplexDataItem value, RemoveMode mode) {
		logger.log(Level.WARNING, "ENTER removeEssenceChange( {1}, {0} )", mode, value);
		try {
			model.getEssenceChanges().stream()
				.filter(vMod -> vMod.getKey().equals(value.getId()))
				.forEach(mod -> model.getEssenceChanges().remove(mod));
			
		ValueModification toRemove = null;
		for (ValueModification vMod : model.getEssenceChanges()) {
			if (vMod.getKey()!=null && vMod.getKey().equals(value.getId())) {
				toRemove = vMod;
				break;
			}
		}
		logger.log(Level.WARNING, "remove essence change {0}",toRemove);

		if (toRemove==null)
			return;
		int pos = model.getEssenceChanges().indexOf(toRemove);

		if (mode==RemoveMode.REMOVE_LATE) {
			ValueModification mod = toValueMod(model, value);
			String lblEssHole = RES.format("label.essencehole_for",  value.getName(Locale.getDefault()));
			int EssHole = (-1 * mod.getValue()); // essence hole gets negative value
			mod = new ValueModification(ShadowrunReference.TEXT, lblEssHole, EssHole);
			logger.log(Level.WARNING, "hole mod = "+mod);
			model.getEssenceChanges().add(mod); // essence hole needs to be added to end of list
		} else {
			model.getEssenceChanges().remove(toRemove); // only remove original line if not REMOVE_LATE
		}
		} finally {
			logger.log(Level.WARNING, "LEAVE removeEssenceChange: now {0}", model.getEssenceChanges());
		}
	}

}
