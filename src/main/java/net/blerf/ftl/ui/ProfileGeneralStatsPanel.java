package net.blerf.ftl.ui;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.blerf.ftl.model.CrewRecord;
import net.blerf.ftl.model.Score;
import net.blerf.ftl.model.Stats;
import net.blerf.ftl.model.Stats.StatType;
import net.blerf.ftl.model.Profile;
import net.blerf.ftl.parser.DataManager;
import net.blerf.ftl.ui.FTLFrame;
import net.blerf.ftl.ui.StatusbarMouseListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ProfileGeneralStatsPanel extends JPanel {

	private static final Logger log = LogManager.getLogger(ProfileGeneralStatsPanel.class);

	private FTLFrame frame;

	private Map<String, BufferedImage> cacheMap = new HashMap<String, BufferedImage>();

	private JPanel topScoresPanel;
	private StatsSubPanel sessionRecordsPanel;
	private StatsSubPanel crewRecordsPanel;
	private StatsSubPanel totalStatsPanel;

	private List<ScorePanel> topScorePanels = new ArrayList<ScorePanel>();


	public ProfileGeneralStatsPanel( FTLFrame frame ) {
		this.frame = frame;

		this.setLayout( new GridLayout(0, 2) );

		topScoresPanel = new JPanel();
		topScoresPanel.setLayout( new BoxLayout(topScoresPanel, BoxLayout.Y_AXIS ) );
		topScoresPanel.setBorder( BorderFactory.createTitledBorder("Top Scores") );
		this.add( topScoresPanel );

		JPanel statsSubPanelsHolder = new JPanel();
		statsSubPanelsHolder.setLayout( new BoxLayout(statsSubPanelsHolder, BoxLayout.Y_AXIS) );
		this.add( statsSubPanelsHolder );
		
		sessionRecordsPanel = new StatsSubPanel();
		sessionRecordsPanel.addFillRow();
		sessionRecordsPanel.setBorder( BorderFactory.createTitledBorder("Session Records") );
		statsSubPanelsHolder.add( sessionRecordsPanel );
		
		crewRecordsPanel = new StatsSubPanel();
		crewRecordsPanel.addFillRow();
		crewRecordsPanel.setBorder( BorderFactory.createTitledBorder("Crew Records") );
		statsSubPanelsHolder.add( crewRecordsPanel );

		totalStatsPanel = new StatsSubPanel();
		totalStatsPanel.addFillRow();
		totalStatsPanel.setBorder( BorderFactory.createTitledBorder("Totals") );
		statsSubPanelsHolder.add( totalStatsPanel );
	}

	public void setProfile( Profile p ) throws IOException {
		topScoresPanel.removeAll();
		topScorePanels.clear();

		StatType[] sessionStatTypes = {StatType.MOST_SHIPS_DEFEATED, StatType.MOST_BEACONS_EXPLORED,
		                               StatType.MOST_SCRAP_COLLECTED, StatType.MOST_CREW_HIRED};
		StatType[] crewStatTypes = {StatType.MOST_REPAIRS, StatType.MOST_COMBAT_KILLS,
		                            StatType.MOST_PILOTED_EVASIONS, StatType.MOST_JUMPS_SURVIVED,
		                            StatType.MOST_SKILL_MASTERIES};
		StatType[] totalStatTypes = {StatType.TOTAL_SHIPS_DEFEATED, StatType.TOTAL_BEACONS_EXPLORED,
		                             StatType.TOTAL_SCRAP_COLLECTED, StatType.TOTAL_CREW_HIRED,
                                 StatType.TOTAL_GAMES_PLAYED, StatType.TOTAL_VICTORIES};
		Stats stats = p.getStats();

		int i = 0;
		for ( Score s : stats.getTopScores() ) {
			ScorePanel tsp = new ScorePanel( ++i, s );
			tsp.setCacheMap( cacheMap );
			topScoresPanel.add( tsp );
			topScorePanels.add( tsp );
		}

		sessionRecordsPanel.removeAll();
		for ( StatType type : sessionStatTypes ) {
			int n = stats.getIntRecord( type );
			sessionRecordsPanel.addRow(type.toString(), null, false, null, n);
		}
		sessionRecordsPanel.addFillRow();

		crewRecordsPanel.removeAll();
		for ( StatType type : crewStatTypes ) {
			CrewRecord r = stats.getCrewRecord( type );
			crewRecordsPanel.addRow(type.toString(), r.getRace(), r.isMale(), r.getName(), r.getValue());
		}
		crewRecordsPanel.addFillRow();

		totalStatsPanel.removeAll();
		for ( StatType type : totalStatTypes ) {
			if ( type == StatType.TOTAL_GAMES_PLAYED )
				totalStatsPanel.addBlankRow();  // Cosmetic spacer.

			int n = stats.getIntRecord( type );
			totalStatsPanel.addRow(type.toString(), null, false, null, n);
		}
		totalStatsPanel.addFillRow();

		this.repaint();
	}

	public void updateProfile( Profile p ) {

		StatType[] sessionStatTypes = {StatType.MOST_SHIPS_DEFEATED, StatType.MOST_BEACONS_EXPLORED,
		                               StatType.MOST_SCRAP_COLLECTED, StatType.MOST_CREW_HIRED};
		StatType[] crewStatTypes = {StatType.MOST_REPAIRS, StatType.MOST_COMBAT_KILLS,
		                            StatType.MOST_PILOTED_EVASIONS, StatType.MOST_JUMPS_SURVIVED,
		                            StatType.MOST_SKILL_MASTERIES};
		StatType[] totalStatTypes = {StatType.TOTAL_SHIPS_DEFEATED, StatType.TOTAL_BEACONS_EXPLORED,
		                             StatType.TOTAL_SCRAP_COLLECTED, StatType.TOTAL_CREW_HIRED,
                                 StatType.TOTAL_GAMES_PLAYED, StatType.TOTAL_VICTORIES};
		Stats stats = p.getStats();

		stats.getTopScores().clear();
		for ( ScorePanel tsp : topScorePanels ) {
			Score s = new Score( tsp.getShipName(), tsp.getShipId(), tsp.getValue(), tsp.getSector(), tsp.getDifficulty(), tsp.isVictory() );
			s.setDLCEnabled( tsp.isDLCEnabled() );
			stats.getTopScores().add(s);
		}

		for ( StatType type : sessionStatTypes ) {
			stats.setIntRecord( type, sessionRecordsPanel.getScore(type.toString()) );
		}
		for ( StatType type : crewStatTypes ) {
			String crewName = crewRecordsPanel.getName(type.toString());
			String race = crewRecordsPanel.getRace(type.toString());
			boolean male = crewRecordsPanel.isMale(type.toString());
			int n = crewRecordsPanel.getScore(type.toString());
			CrewRecord r = new CrewRecord( crewName, race, male, n );
			stats.setCrewRecord( type, r );
		}
		for ( StatType type : totalStatTypes ) {
			stats.setIntRecord( type, totalStatsPanel.getScore(type.toString()) );
		}
	}
}
