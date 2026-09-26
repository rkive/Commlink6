package de.rpgframework.shadowrun6.chargen.gen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.rpgframework.shadowrun.Priority;
import de.rpgframework.shadowrun.PriorityType;
import de.rpgframework.shadowrun.SIN;
import de.rpgframework.shadowrun.SIN.FakeRating;
import de.rpgframework.shadowrun.chargen.charctrl.SINController;
import de.rpgframework.shadowrun6.Shadowrun6Character;
import de.rpgframework.shadowrun6.chargen.gen.priority.PriorityCharacterGenerator;
import de.rpgframework.shadowrun6.data.Shadowrun6DataPlugin;

public class SINControllerTest {

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
		charGen.getPriorityController().setPriority(PriorityType.RESOURCES, Priority.A);
	}

	@Test
	public void testDeleteSINRemovesItsLicenses() {
		SINController sinCtrl = charGen.getSINController();
		SIN kept = sinCtrl.createNewSIN("Kept", FakeRating.SUPERFICIALLY_PLAUSIBLE);
		sinCtrl.createNewLicense(kept, FakeRating.SUPERFICIALLY_PLAUSIBLE, "Driver's License");
		SIN deleted = sinCtrl.createNewSIN("Deleted", FakeRating.SUPERFICIALLY_PLAUSIBLE);
		sinCtrl.createNewLicense(deleted, FakeRating.SUPERFICIALLY_PLAUSIBLE, "Concealed Carry License");
		sinCtrl.createNewLicense(deleted, FakeRating.SUPERFICIALLY_PLAUSIBLE, "Cyberware License");

		assertTrue(sinCtrl.deleteSIN(deleted));

		assertTrue(model.getLicenses(deleted).isEmpty());
		assertEquals(1, model.getLicenses().size());
		assertEquals(1, model.getLicenses(kept).size());
	}

}
