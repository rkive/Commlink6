package de.rpgframework.shadowrun6.chargen.charctrl;

import java.lang.System.Logger.Level;
import java.util.UUID;

import de.rpgframework.shadowrun.LicenseValue;
import de.rpgframework.shadowrun.SIN;
import de.rpgframework.shadowrun.SIN.FakeRating;
import de.rpgframework.shadowrun.chargen.charctrl.SINController;

/**
 * @author prelle
 *
 */
public abstract class CommonSINController extends ControllerImpl<de.rpgframework.shadowrun.SIN> implements SINController {

	//-------------------------------------------------------------------
	protected CommonSINController(SR6CharacterController parent) {
		super(parent);
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.shadowrun.chargen.charctrl.SINController#canCreateNewSIN(de.rpgframework.shadowrun.SIN.FakeRating)
	 */
	@Override
	public boolean canCreateNewSIN(FakeRating quality) {
		int cost = quality.getValue() *2500;
		return getModel().getNuyen()>=cost;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.shadowrun.chargen.charctrl.SINController#canCreateNewSIN(de.rpgframework.shadowrun.SIN.FakeRating, int)
	 */
	@Override
	public boolean canCreateNewSIN(FakeRating quality, int count) {
		int cost = quality.getValue() *2500 * count;
		return getModel().getNuyen()>=cost;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.shadowrun.chargen.charctrl.SINController#canDeleteSIN(de.rpgframework.shadowrun.SIN)
	 */
	@Override
	public boolean canDeleteSIN(SIN value) {
		if (value.getQuality()==FakeRating.REAL_SIN) return false;
		return getModel().getSINs().contains(value);
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.shadowrun.chargen.charctrl.SINController#createNewSIN(java.lang.String, de.rpgframework.shadowrun.SIN.FakeRating)
	 */
	@Override
	public SIN createNewSIN(String name, FakeRating quality) {
		if (!canCreateNewSIN(quality)) {
			logger.log(Level.ERROR, "Tried to create a rating "+quality.getValue()+" SIN, but I may not");
			return null;
		}
		
		SIN sin = new SIN(quality);
		sin.setName(name);
		sin.setUniqueId(UUID.randomUUID());
		getModel().addSIN(sin);
		logger.log(Level.WARNING, "Added new SIN ''{0}'' of quality {1}", name, quality);
		
		parent.runProcessors();
		return sin;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.shadowrun.chargen.charctrl.SINController#createNewSIN(java.lang.String, de.rpgframework.shadowrun.SIN.FakeRating, int)
	 */
	@Override
	public SIN[] createNewSIN(FakeRating quality, int count) {
		if (!canCreateNewSIN(quality, count)) {
			logger.log(Level.ERROR, "Tried to create "+count+" rating "+quality.getValue()+" SINs, but I may not");
			return null;
		}
		
		SIN[] ret = new SIN[count];
		for (int i=0; i<count; i++) {
		SIN sin = new SIN(quality);
		sin.setName("Unnamed");
		sin.setUniqueId(UUID.randomUUID());
		getModel().addSIN(sin);
		}
		logger.log(Level.INFO, "Added {0} new SINs of quality {1}", count, quality);
		
		parent.runProcessors();
		return ret;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.shadowrun.chargen.charctrl.SINController#deleteSIN(de.rpgframework.shadowrun.SIN)
	 */
	@Override
	public boolean deleteSIN(SIN data) {
		if (!canDeleteSIN(data))
			return false;
		
		getModel().removeSIN(data);
		logger.log(Level.INFO, "Removed SIN {0}", data);
		
		// Remove all licenses associated with this SIN
		for (LicenseValue val : getModel().getLicenses(data)) {
			logger.log(Level.INFO, "Remove license ''{0}'' associated with SIN", val.getNameWithRating());
			getModel().removeLicense(val);
		}
		
		parent.runProcessors();
		return true;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.shadowrun.chargen.charctrl.SINController#canCreateNewLicense(de.rpgframework.shadowrun.LicenseType, de.rpgframework.shadowrun.SIN, de.rpgframework.shadowrun.SIN.FakeRating)
	 */
	@Override
	public boolean canCreateNewLicense(SIN sin, FakeRating quality) {
		int cost = quality.getValue() *200;
		return getModel().getNuyen()>=cost;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.shadowrun.chargen.charctrl.SINController#createNewLicense(de.rpgframework.shadowrun.LicenseType, de.rpgframework.shadowrun.SIN, de.rpgframework.shadowrun.SIN.FakeRating, java.lang.String)
	 */
	@Override
	public LicenseValue createNewLicense(SIN sin, FakeRating quality, String name) {
		if (!canCreateNewLicense(sin, quality)) {
			return null;
		}
		
		LicenseValue ret = new LicenseValue(name, quality);
		if (sin!=null) {
			ret.setSIN(sin.getUniqueId());
		}
		logger.log(Level.INFO, "Added new license ''{0}'' of quality", name, quality);
		getModel().addLicense(ret);
		
		parent.runProcessors();
		return ret;
	}

	//-------------------------------------------------------------------
	/**
	 * @see de.rpgframework.shadowrun.chargen.charctrl.SINController#deleteLicense(de.rpgframework.shadowrun.LicenseValue)
	 */
	@Override
	public boolean deleteLicense(LicenseValue data) {
		boolean ret = getModel().removeLicense(data);
		logger.log(Level.INFO, "Removed license ''{0}'' from SIN {1}", data.getName(), data.getSIN());
		if (ret) {
			parent.runProcessors();			
		}
		return ret;
	}

}
