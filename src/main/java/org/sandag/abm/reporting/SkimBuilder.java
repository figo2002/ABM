package org.sandag.abm.reporting;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sandag.abm.accessibilities.AutoAndNonMotorizedSkimsCalculator;
import org.sandag.abm.accessibilities.AutoTazSkimsCalculator;
import org.sandag.abm.accessibilities.BestTransitPathCalculator;
import org.sandag.abm.accessibilities.DriveTransitWalkSkimsCalculator;
import org.sandag.abm.accessibilities.WalkTransitDriveSkimsCalculator;
import org.sandag.abm.accessibilities.WalkTransitWalkSkimsCalculator;
import org.sandag.abm.ctramp.ModelStructure;
import org.sandag.abm.modechoice.MgraDataManager;
import org.sandag.abm.modechoice.Modes;
import org.sandag.abm.modechoice.TapDataManager;
import org.sandag.abm.modechoice.TazDataManager;

/**
 * The {@code SkimBuilder} ...
 * 
 * @author crf Started 10/17/12 9:12 AM
 */
public class SkimBuilder
{
    private static final Logger                      LOGGER                                 = Logger.getLogger(SkimBuilder.class);

    private static final int                         WALK_TIME_INDEX                        = 0;
    private static final int                         BIKE_TIME_INDEX                        = 0;

    private static final int                         DA_TIME_INDEX                          = 0;
    private static final int                         DA_FF_TIME_INDEX                       = 1;
    private static final int                         DA_DIST_INDEX                          = 2;
    private static final int                         DA_TOLL_TIME_INDEX                     = 3;
    private static final int                         DA_TOLL_FF_TIME_INDEX                  = 4;
    private static final int                         DA_TOLL_DIST_INDEX                     = 5;
    private static final int                         DA_TOLL_COST_INDEX                     = 6;
    private static final int                         DA_TOLL_TOLLDIST_INDEX                 = 7;
    private static final int                         SR2_TIME_INDEX                         = 8;
    private static final int                         SR2_FF_TIME_INDEX                      = 9;
    private static final int                         SR2_DIST_INDEX                         = 10;
    private static final int                         SR2_HOVDIST_INDEX                      = 11;
    private static final int                         SR2_TOLL_TIME_INDEX                    = 12;
    private static final int                         SR2_TOLL_FF_TIME_INDEX                 = 13;
    private static final int                         SR2_TOLL_DIST_INDEX                    = 14;
    private static final int                         SR2_TOLL_COST_INDEX                    = 15;
    private static final int                         SR2_TOLL_TOLLDIST_INDEX                = 16;
    private static final int                         SR3_TIME_INDEX                         = 17;
    private static final int                         SR3_FF_TIME_INDEX                      = 18;
    private static final int                         SR3_DIST_INDEX                         = 19;
    private static final int                         SR3_HOVDIST_INDEX                      = 20;
    private static final int                         SR3_TOLL_TIME_INDEX                    = 21;
    private static final int                         SR3_TOLL_FF_TIME_INDEX                 = 22;
    private static final int                         SR3_TOLL_DIST_INDEX                    = 23;
    private static final int                         SR3_TOLL_COST_INDEX                    = 24;
    private static final int                         SR3_TOLL_TOLLDIST_INDEX                = 25;
    private static final int                         DA_STD_INDEX                           = 26;
    private static final int                         DA_TOLL_STD_INDEX                      = 27;
    private static final int                         SR2_STD_INDEX                          = 28;
    private static final int                         SR2_TOLL_STD_INDEX                     = 29;
    private static final int                         SR3_STD_INDEX                          = 30;
    private static final int                         SR3_TOLL_STD_INDEX                     = 31;
    
    private static final int                         TRANSIT_LOCAL_ACCESS_TIME_INDEX        = 0;
    private static final int                         TRANSIT_LOCAL_EGRESS_TIME_INDEX        = 1;
    private static final int                         TRANSIT_LOCAL_AUX_WALK_TIME_INDEX      = 2;
    private static final int                         TRANSIT_LOCAL_IN_VEHICLE_TIME_INDEX    = 3;
    private static final int                         TRANSIT_LOCAL_FIRST_WAIT_TIME_INDEX    = 4;
    private static final int                         TRANSIT_LOCAL_TRANSFER_WAIT_TIME_INDEX = 5;
    private static final int                         TRANSIT_LOCAL_FARE_INDEX               = 6;
    private static final int                         TRANSIT_LOCAL_XFERS_INDEX              = 7;

    private static final int                         TRANSIT_PREM_ACCESS_TIME_INDEX         = 0;
    private static final int                         TRANSIT_PREM_EGRESS_TIME_INDEX         = 1;
    private static final int                         TRANSIT_PREM_AUX_WALK_TIME_INDEX       = 2;
    private static final int                         TRANSIT_PREM_LOCAL_BUS_TIME_INDEX      = 3;
    private static final int                         TRANSIT_PREM_EXPRESS_BUS_TIME_INDEX    = 4;
    private static final int                         TRANSIT_PREM_BRT_TIME_INDEX            = 5;
    private static final int                         TRANSIT_PREM_LRT_TIME_INDEX            = 6;
    private static final int                         TRANSIT_PREM_CR_TIME_INDEX             = 7;
    private static final int                         TRANSIT_PREM_FIRST_WAIT_TIME_INDEX     = 8;
    private static final int                         TRANSIT_PREM_TRANSFER_WAIT_TIME_INDEX  = 9;
    private static final int                         TRANSIT_PREM_FARE_INDEX                = 10;
    private static final int                         TRANSIT_MAIN_MODE_INDEX                = 11;
    private static final int                         TRANSIT_PREM_XFERS_INDEX               = 12;
    
    private static final double                      FEET_IN_MILE                           = 5280.0;

    private final TapDataManager                     tapManager;
    private final TazDataManager                     tazManager;
    private final MgraDataManager                    mgraManager;
    private final AutoTazSkimsCalculator             tazDistanceCalculator;
    private final AutoAndNonMotorizedSkimsCalculator autoNonMotSkims;
    private final WalkTransitWalkSkimsCalculator     wtw;
    private final WalkTransitDriveSkimsCalculator    wtd;
    private final DriveTransitWalkSkimsCalculator    dtw;
    
    private final String 			FUEL_COST_PROPERTY          = "aoc.fuel";
    private final String 			MAINTENANCE_COST_PROPERTY = "aoc.maintenance";
    private float autoOperatingCost;
    

    public SkimBuilder(Properties properties)
    {

        HashMap<String, String> rbMap = new HashMap<String, String>(
                (Map<String, String>) (Map) properties);
        tapManager = TapDataManager.getInstance(rbMap);
        tazManager = TazDataManager.getInstance(rbMap);
        mgraManager = MgraDataManager.getInstance(rbMap);
        tazDistanceCalculator = new AutoTazSkimsCalculator(rbMap);
        tazDistanceCalculator.computeTazDistanceArrays();
        autoNonMotSkims = new AutoAndNonMotorizedSkimsCalculator(rbMap);
        autoNonMotSkims.setTazDistanceSkimArrays(
                tazDistanceCalculator.getStoredFromTazToAllTazsDistanceSkims(),
                tazDistanceCalculator.getStoredToTazFromAllTazsDistanceSkims());

        BestTransitPathCalculator bestPathUEC = new BestTransitPathCalculator(rbMap);
        wtw = new WalkTransitWalkSkimsCalculator();
        wtw.setup(rbMap, LOGGER, bestPathUEC);
        wtd = new WalkTransitDriveSkimsCalculator();
        wtd.setup(rbMap, LOGGER, bestPathUEC);
        dtw = new DriveTransitWalkSkimsCalculator();
        dtw.setup(rbMap, LOGGER, bestPathUEC);
        
        float fuelCost = new Float(properties.getProperty(FUEL_COST_PROPERTY));
        float mainCost = new Float(properties.getProperty(MAINTENANCE_COST_PROPERTY));
        autoOperatingCost = (fuelCost + mainCost)  * 0.01f;

    }

    // todo: hard coding these next two lookups because it is convenient, but
    // probably should move to a lookup file
    private final String[]         modeNameLookup   = {
            "UNKNOWN", // ids start at one
            "DRIVEALONEFREE", "DRIVEALONEPAY", "SHARED2GP", "SHARED2HOV", "SHARED2PAY",
            "SHARED3GP", "SHARED3HOV", "SHARED3PAY", "WALK", "BIKE", "WALK_LOC", "WALK_EXP",
            "WALK_BRT", "WALK_LR", "WALK_CR", "PNR_LOC", "PNR_EXP", "PNR_BRT", "PNR_LR", "PNR_CR",
            "KNR_LOC", "KNR_EXP", "KNR_BRT", "KNR_LR", "KNR_CR", "SCHBUS", "TAXI"};

    private final TripModeChoice[] modeChoiceLookup = {TripModeChoice.UNKNOWN,
            TripModeChoice.DRIVE_ALONE_NO_TOLL, TripModeChoice.DRIVE_ALONE_TOLL,
            TripModeChoice.DRIVE_ALONE_NO_TOLL, TripModeChoice.SR2_HOV, TripModeChoice.SR2_TOLL,
            TripModeChoice.DRIVE_ALONE_NO_TOLL, TripModeChoice.SR3_HOV, TripModeChoice.SR3_TOLL,
            TripModeChoice.WALK, TripModeChoice.BIKE, TripModeChoice.WALK_LB,
            TripModeChoice.WALK_EB, TripModeChoice.WALK_BRT, TripModeChoice.WALK_LRT,
            TripModeChoice.WALK_CR, TripModeChoice.DRIVE_LB, TripModeChoice.DRIVE_EB,
            TripModeChoice.DRIVE_BRT, TripModeChoice.DRIVE_LRT, TripModeChoice.DRIVE_CR,
            TripModeChoice.DRIVE_LB, TripModeChoice.DRIVE_EB, TripModeChoice.DRIVE_BRT,
            TripModeChoice.DRIVE_LRT, TripModeChoice.DRIVE_CR, TripModeChoice.DRIVE_ALONE_NO_TOLL,
            TripModeChoice.DRIVE_ALONE_NO_TOLL      };

    private int getTod(int tripTimePeriod)
    {
        return ModelStructure.getSkimPeriodIndex(tripTimePeriod);
    }

    private int getStartTime(int tripTimePeriod)
    {
        return (tripTimePeriod - 1) * 30 + 270; // starts at 4:30 and goes half
                                                // hour intervals after that
    }

    public TripAttributes getTripAttributes(int origin, int destination, int tripModeIndex,
            int boardTap, int alightTap, int tripTimePeriod, boolean inbound, float valueOfTime)
    {
        int tod = getTod(tripTimePeriod);
        TripModeChoice tripMode = modeChoiceLookup[tripModeIndex < 0 ? 0 : tripModeIndex];
       
        TripAttributes attributes = getTripAttributes(tripMode, origin, destination, boardTap,
                alightTap, tod, inbound, valueOfTime);
        attributes.setTripModeName(modeNameLookup[tripModeIndex < 0 ? 0 : tripModeIndex]);
        attributes.setTripStartTime(getStartTime(tripTimePeriod));
        return attributes;
    }

    private TripAttributes getTripAttributesUnknown()
    {
        return new TripAttributes(0,0,0,0,0,0,0,0,0,0,0,-1,-1,0);
    }

    private final float DEFAULT_BIKE_SPEED = 12;
    private final float DEFAULT_WALK_SPEED = 3;

    private TripAttributes getTripAttributes(TripModeChoice modeChoice, int origin,
            int destination, int boardTap, int alightTap, int tod, boolean inbound, float vot)
    {
        int timeIndex = -1;
        int distIndex = -1;
        int costIndex = -1;
        int stdIndex  = -1;

        int rideModeIndex = modeChoice.getRideModeIndex();

        switch (modeChoice)
        {
            case UNKNOWN:
                return getTripAttributesUnknown();
            case DRIVE_ALONE_NO_TOLL:
            {
                timeIndex = DA_TIME_INDEX;
                distIndex = DA_DIST_INDEX;
                costIndex = -1;
                stdIndex  = DA_STD_INDEX;
                double[] autoSkims = autoNonMotSkims.getAutoSkims(origin, destination, tod, vot,false,
                        LOGGER);
                return new TripAttributes(autoSkims[timeIndex], autoSkims[distIndex], autoSkims[distIndex]*autoOperatingCost, autoSkims[stdIndex]);
            }
            case DRIVE_ALONE_TOLL:
            {
                timeIndex = DA_TOLL_TIME_INDEX;
                distIndex = DA_TOLL_DIST_INDEX;
                costIndex = DA_TOLL_COST_INDEX;
                stdIndex  = DA_TOLL_STD_INDEX;
               double[] autoSkims = autoNonMotSkims.getAutoSkims(origin, destination, tod, vot,false,
                        LOGGER);
                
                //if IVT for toll is zero, get non-toll skim
                if(autoSkims[timeIndex]==0){
                    getTripAttributes(TripModeChoice.DRIVE_ALONE_NO_TOLL, origin,
                            destination, boardTap,  alightTap,  tod,  inbound,  vot);
                }
                return new TripAttributes(autoSkims[timeIndex], autoSkims[distIndex], autoSkims[distIndex]*autoOperatingCost, autoSkims[stdIndex], autoSkims[costIndex]);
            }
            case SR2_HOV: // wu added
            {
                timeIndex = SR2_TIME_INDEX;
                distIndex = SR2_DIST_INDEX;
                costIndex = -1;
                stdIndex  = SR2_STD_INDEX;               
                double[] autoSkims = autoNonMotSkims.getAutoSkims(origin, destination, tod, vot,false,
                        LOGGER);
                //if IVT for HOV is zero, get non-toll skim
                if(autoSkims[timeIndex]==0){
                    getTripAttributes(TripModeChoice.DRIVE_ALONE_NO_TOLL, origin,
                            destination, boardTap,  alightTap,  tod,  inbound,  vot);
                }
                return new TripAttributes(autoSkims[timeIndex], autoSkims[distIndex], autoSkims[distIndex]*autoOperatingCost, autoSkims[stdIndex]);
                            }
            case SR2_TOLL: // wu added
            {
                timeIndex = SR2_TOLL_TIME_INDEX;
                distIndex = SR2_TOLL_DIST_INDEX;
                costIndex = SR2_TOLL_COST_INDEX;
                stdIndex  = SR2_TOLL_STD_INDEX;
                double[] autoSkims = autoNonMotSkims.getAutoSkims(origin, destination, tod, vot,false,
                        LOGGER);
                //if IVT for toll is zero, get non-toll HOV skim
                if(autoSkims[timeIndex]==0){
                    getTripAttributes(TripModeChoice.SR2_HOV, origin,
                            destination, boardTap,  alightTap,  tod,  inbound,  vot);
                }
                return new TripAttributes(autoSkims[timeIndex], autoSkims[distIndex], autoSkims[distIndex]*autoOperatingCost, autoSkims[stdIndex], autoSkims[costIndex]);
                            }
            case SR3_HOV: // wu added
            {
                timeIndex = SR3_TIME_INDEX;
                distIndex = SR3_DIST_INDEX;
                costIndex = -1;
                stdIndex  = SR2_STD_INDEX;
                double[] autoSkims = autoNonMotSkims.getAutoSkims(origin, destination, tod, vot,false,
                        LOGGER);
                //if IVT for HOV is zero, get non-HOV skim
                if(autoSkims[timeIndex]==0){
                    getTripAttributes(TripModeChoice.DRIVE_ALONE_NO_TOLL, origin,
                            destination, boardTap,  alightTap,  tod,  inbound,  vot);
                }
                return new TripAttributes(autoSkims[timeIndex], autoSkims[distIndex], autoSkims[distIndex]*autoOperatingCost, autoSkims[stdIndex]);
            }
            case SR3_TOLL:
            {
                timeIndex = SR3_TOLL_TIME_INDEX;
                distIndex = SR3_TOLL_DIST_INDEX;
                costIndex = SR3_TOLL_COST_INDEX;
                stdIndex  = SR3_TOLL_STD_INDEX;
                double[] autoSkims = autoNonMotSkims.getAutoSkims(origin, destination, tod, vot,false,
                        LOGGER);
                //if IVT for toll is zero, get non-toll HOV skim
                if(autoSkims[timeIndex]==0){
                    getTripAttributes(TripModeChoice.SR3_HOV, origin,
                            destination, boardTap,  alightTap,  tod,  inbound,  vot);
                }
                return new TripAttributes(autoSkims[timeIndex], autoSkims[distIndex], autoSkims[distIndex]*autoOperatingCost, autoSkims[stdIndex], autoSkims[costIndex]);
            }
            case WALK:
            {
                // first, look in mgra manager, otherwise default to auto skims
                double distance = mgraManager.getMgraToMgraWalkDistFrom(origin, destination) / FEET_IN_MILE;
                double time =0;
                if (distance > 0)
                {
                    time = mgraManager.getMgraToMgraWalkTime(origin, destination);
                }else{
                	distance = autoNonMotSkims.getAutoSkims(origin, destination, tod, vot,false, LOGGER)[DA_DIST_INDEX];
                	time = distance * 60 / DEFAULT_WALK_SPEED;
                }
                return new TripAttributes(0, 0, 0, 0, 0, 0, 0, 0, time, 0, distance, -1, -1, 0);
            }
            case BIKE:
            {
                double time = mgraManager.getMgraToMgraBikeTime(origin, destination);
                double distance = 0;
                if (time > 0)
                {
                    distance = time * DEFAULT_BIKE_SPEED / 60;
                   
                }else{
                	distance = autoNonMotSkims.getAutoSkims(origin, destination, tod, vot,false,
                        LOGGER)[DA_DIST_INDEX];
                	time = distance * 60 / DEFAULT_BIKE_SPEED;
                }
                return new TripAttributes(0, 0, 0, 0, 0, 0, 0, 0, 0, time, distance, -1, -1, 0);
            }
            case WALK_LB:
            case WALK_EB:
            case WALK_BRT:
            case WALK_LRT:
            case WALK_CR:
            case DRIVE_LB:
            case DRIVE_EB:
            case DRIVE_BRT:
            case DRIVE_LRT:
            case DRIVE_CR:
            {
                boolean isDrive = modeChoice.isDrive;
                boolean isPremium = modeChoice.isPremium;

                double[] skims;
                int boardTaz = -1;
                int alightTaz = -1;
                double walkTime = 0.0;
                double driveTime = 0.0;
                
                boardTaz = mgraManager.getTaz(origin);
                alightTaz = mgraManager.getTaz(destination);
                if (isDrive)
                {
                    if (!inbound)
                    { // outbound: drive to transit stop at origin, then transit
                      // to destination
                        int taz = tapManager.getTazForTap(boardTap);
                        boardTaz = taz;
                        int btapPosition = tazManager.getTapPosition(taz, boardTap,
                                Modes.AccessMode.PARK_N_RIDE);
                        int atapPosition = mgraManager.getTapPosition(destination, alightTap);
                        if (atapPosition < 0 || btapPosition < 0)
                        {
                            LOGGER.info("bad tap position for drive access board tap");
                            LOGGER.info("mc: " + modeChoice);
                            LOGGER.info("origin: " + origin);
                            LOGGER.info("dest: " + destination);
                            LOGGER.info("board tap: " + boardTap);
                            LOGGER.info("alight tap: " + alightTap);
                            LOGGER.info("tod: " + tod);
                            LOGGER.info("inbound: " + inbound);
                            LOGGER.info("board tap position: " + btapPosition);
                            LOGGER.info("alight tap position: " + atapPosition);
                        } else
                        {
                            driveTime = tazManager.getTapTime(taz, btapPosition,
                                    Modes.AccessMode.PARK_N_RIDE);
                            walkTime = mgraManager.getMgraToTapWalkTime(destination,
                                    atapPosition);
                        }
                        skims = dtw.getDriveTransitWalkSkims(rideModeIndex, driveTime,
                                walkTime, boardTap, alightTap, tod, false);
                    } else
                    { // inbound: transit from origin to destination, then drive
                        int taz = -1;
                        try
                        {
                            taz = tapManager.getTazForTap(alightTap);
                            alightTaz = taz;
                        } catch (NullPointerException e)
                        {
                            LOGGER.info("tap manager can't find taz for alight tap");
                            LOGGER.info("mc: " + modeChoice);
                            LOGGER.info("origin: " + origin);
                            LOGGER.info("dest: " + destination);
                            LOGGER.info("board tap: " + boardTap);
                            LOGGER.info("alight tap: " + alightTap);
                            LOGGER.info("tod: " + tod);
                            LOGGER.info("inbound: " + inbound);
                            LOGGER.info("a: " + tapManager.getTapParkingInfo());
                            LOGGER.info("b: " + tapManager.getTapParkingInfo()[alightTap]);
                            LOGGER.info("b: " + tapManager.getTapParkingInfo()[alightTap][1]);
                            LOGGER.info("b: " + tapManager.getTapParkingInfo()[alightTap][1][0]);
                            throw e;
                        }
                        int atapPosition = tazManager.getTapPosition(taz, alightTap,
                                Modes.AccessMode.PARK_N_RIDE);
                        int btapPosition = mgraManager.getTapPosition(origin, boardTap);
                        if (atapPosition < 0 || btapPosition < 0)
                        {

                            LOGGER.info("mc: " + modeChoice);
                            LOGGER.info("origin: " + origin);
                            LOGGER.info("dest: " + destination);
                            LOGGER.info("board tap: " + boardTap);
                            LOGGER.info("alight tap: " + alightTap);
                            LOGGER.info("tod: " + tod);
                            LOGGER.info("inbound: " + inbound);
                            LOGGER.info("board tap position: " + btapPosition);
                            LOGGER.info("alight tap position: " + atapPosition);
                        } else
                        {
                            walkTime = mgraManager
                                    .getMgraToTapWalkTime(origin, btapPosition);
                            driveTime = tazManager.getTapTime(taz, atapPosition,
                                    Modes.AccessMode.PARK_N_RIDE);
                        }
                        skims = wtd.getWalkTransitDriveSkims(rideModeIndex, walkTime,
                                driveTime, boardTap, alightTap, tod, false);
                    }
                } else
                {
                    int bt = mgraManager.getTapPosition(origin, boardTap);
                    int at = mgraManager.getTapPosition(destination, alightTap);
                    float boardWalkTime=0f;
                    float alightWalkTime=0f;
                    
                    if (bt < 0 || at < 0)
                    {
                        LOGGER.info("bad tap position: " + bt + "  " + at);
                        LOGGER.info("mc: " + modeChoice);
                        LOGGER.info("origin: " + origin);
                        LOGGER.info("dest: " + destination);
                        LOGGER.info("board tap: " + boardTap);
                        LOGGER.info("alight tap: " + alightTap);
                        LOGGER.info("tod: " + tod);
                        LOGGER.info("inbound: " + inbound);
                        LOGGER.info("board tap position: " + bt);
                        LOGGER.info("alight tap position: " + at);
                    } else
                    {
                        boardWalkTime = mgraManager.getMgraToTapWalkTime(origin, bt);
                        alightWalkTime = mgraManager.getMgraToTapWalkTime(destination, at);
                        walkTime = boardWalkTime + alightWalkTime;
                    }
                    skims = wtw.getWalkTransitWalkSkims(rideModeIndex, boardWalkTime,
                            alightWalkTime, boardTap, alightTap, tod, false);
                }

                //Wu modified to get the correct in vehicle time
                double transitInVehicleTime = 0.0;
                if(isPremium){
                	transitInVehicleTime = skims[TRANSIT_PREM_LOCAL_BUS_TIME_INDEX]+skims[TRANSIT_PREM_CR_TIME_INDEX]+skims[TRANSIT_PREM_LRT_TIME_INDEX]+skims[TRANSIT_PREM_BRT_TIME_INDEX]+skims[TRANSIT_PREM_EXPRESS_BUS_TIME_INDEX];               	
                }else{
                	transitInVehicleTime = skims[TRANSIT_LOCAL_IN_VEHICLE_TIME_INDEX];
                	
                }

                double transitFare = skims[isPremium ? TRANSIT_PREM_FARE_INDEX
                        : TRANSIT_LOCAL_FARE_INDEX];
                
                
                walkTime += skims[isPremium ? TRANSIT_PREM_AUX_WALK_TIME_INDEX
                        : TRANSIT_LOCAL_AUX_WALK_TIME_INDEX];
                
                double waitTime = 0.0;
                waitTime += skims[isPremium ? TRANSIT_PREM_FIRST_WAIT_TIME_INDEX
                        : TRANSIT_LOCAL_FIRST_WAIT_TIME_INDEX];
                waitTime += skims[isPremium ? TRANSIT_PREM_TRANSFER_WAIT_TIME_INDEX
                        : TRANSIT_LOCAL_TRANSFER_WAIT_TIME_INDEX];
                double dist = autoNonMotSkims.getAutoSkims(origin, destination, tod, vot, false, LOGGER)[DA_DIST_INDEX]; 
            
                //assuming 35 mph drive speed for transit access
                return new TripAttributes(driveTime, driveTime/60*35*autoOperatingCost, 0, 0,  transitInVehicleTime, 
                		waitTime, walkTime, transitFare, 0, 0, dist, boardTaz, alightTaz, vot);
            }
            default:
                throw new IllegalStateException("Should not be here: " + modeChoice);
        }
    }

    public static enum TripModeChoice
    {
        UNKNOWN(), DRIVE_ALONE_NO_TOLL(false), DRIVE_ALONE_TOLL(true), SR2_GP(false), SR2_HOV(false), SR2_TOLL(
                true), SR3_GP(false), SR3_HOV(false), SR3_TOLL(true), WALK(), BIKE(), WALK_LB(Modes
                .getTransitModeIndex("LB"), false, false), WALK_EB(Modes.getTransitModeIndex("EB"),
                true, false), WALK_BRT(Modes.getTransitModeIndex("BRT"), true, false), WALK_LRT(
                Modes.getTransitModeIndex("LR"), true, false), WALK_CR(Modes
                .getTransitModeIndex("CR"), true, false), DRIVE_LB(Modes.getTransitModeIndex("LB"),
                false, true), DRIVE_EB(Modes.getTransitModeIndex("EB"), true, true), DRIVE_BRT(
                Modes.getTransitModeIndex("BRT"), true, true), DRIVE_LRT(Modes
                .getTransitModeIndex("LR"), true, true), DRIVE_CR(Modes.getTransitModeIndex("CR"),
                true, true);

        private final int     rideModeIndex;
        private final boolean isPremium;
        private final boolean isDrive;
        private final boolean isToll;

        private TripModeChoice(int rideModeIndex, boolean premium, boolean drive, boolean toll)
        {
            this.rideModeIndex = rideModeIndex;
            isPremium = premium;
            isDrive = drive;
            isToll = toll;
        }

        private TripModeChoice(int rideModeIndex, boolean premium, boolean drive)
        {
            this(rideModeIndex, premium, drive, false);
        }

        private TripModeChoice(boolean isToll)
        {
            this(-1, false, false, isToll);
        }

        private TripModeChoice()
        {
            this(-1, false, false);
        }

        private int getRideModeIndex()
        {
            return rideModeIndex;
        }
    }

    public static class TripAttributes
    {
        private final float autoInVehicleTime;
        private final float autoOperatingCost;
        private final float autoStandardDeviationTime;
        private final float autoTollCost;
        private final float transitInVehicleTime;
        private final float transitWaitTime;
        private final float transitWalkTime;
        private final float transitFare;
        private final float walkModeTime;
        private final float bikeModeTime;
        private final float tripDistance;
        private final int   tripBoardTaz;
        private final int   tripAlightTaz;
        private final float valueOfTime;

        private String      tripModeName;

        public int getTripStartTime()
        {
            return tripStartTime;
        }

        public void setTripStartTime(int tripStartTime)
        {
            this.tripStartTime = tripStartTime;
        }

        private int tripStartTime;

        public TripAttributes(double autoInVehicleTime, double autoOperatingCost, double autoStandardDeviationTime, double autoTollCost, double transitInVehicleTime, 
        		double transitWaitTime, double transitWalkTime, double transitFare, double walkModeTime, double bikeModeTime, double tripDistance,
        		int tripBoardTaz, int tripAlightTaz, float valueOfTime)
        {
            this.autoInVehicleTime = (float) autoInVehicleTime;
            this.autoOperatingCost = (float)  autoOperatingCost;
            this.autoStandardDeviationTime = (float) autoStandardDeviationTime;
            this.autoTollCost = (float) autoTollCost;
            this.transitInVehicleTime = (float) transitInVehicleTime;
            this.transitWaitTime = (float) transitWaitTime;
            this.transitWalkTime = (float) transitWalkTime;
            this.transitFare = (float) transitFare;
            this.walkModeTime = (float) walkModeTime;
            this.bikeModeTime = (float) bikeModeTime;
            this.tripDistance = (float) tripDistance;
            this.tripBoardTaz = tripBoardTaz;
            this.tripAlightTaz = tripAlightTaz;
            this.valueOfTime = valueOfTime;
        }

        /**
         * A method to set create trip attributes for a non-toll auto choice.
         * 
         * @param autoInVehicleTime
         * @param tripDistance
         * @param autoOperatingCost
         */
        public TripAttributes(double autoInVehicleTime, double tripDistance, double autoOperatingCost, double stdDevTime)
        {
            this(autoInVehicleTime, autoOperatingCost, stdDevTime, 0,0,0,0,0,0,0,tripDistance,-1,-1,0);
        }

        /**
         * A method to create trip attributes for a toll auto choice.
         * 
         * @param autoInVehicleTime
         * @param tripDistance
         * @param autoOperatingCost
         * @param tollCost
         */
        public TripAttributes(double autoInVehicleTime, double tripDistance, double autoOperatingCost, double stdDevTime, double tollCost)
        {
            this(autoInVehicleTime, autoOperatingCost, stdDevTime, tollCost,0,0,0,0,0,0,tripDistance,-1,-1,0);
        }
       
         
        
        public void setTripModeName(String tripModeName)
        {
            this.tripModeName = tripModeName;
        }

        public float getAutoInVehicleTime() {
			return autoInVehicleTime;
		}

		public float getAutoOperatingCost() {
			return autoOperatingCost;
		}

		public float getAutoStandardDeviationTime() {
			return autoStandardDeviationTime;
		}

		public float getAutoTollCost() {
			return autoTollCost;
		}

		public float getTransitInVehicleTime() {
			return transitInVehicleTime;
		}

		public float getTransitWaitTime() {
			return transitWaitTime;
		}

		public float getTransitFare() {
			return transitFare;
		}

		public float getTransitWalkTime() {
			return transitWalkTime;
		}

		public float getWalkModeTime() {
			return walkModeTime;
		}

		public float getBikeModeTime() {
			return bikeModeTime;
		}

		public float getTripDistance() {
			return tripDistance;
		}

		public String getTripModeName()
        {
            return tripModeName;
        }

        public int getTripBoardTaz()
        {
            return tripBoardTaz;
        }

        public int getTripAlightTaz()
        {
            return tripAlightTaz;
        }

		public float getValueOfTime() {
			return valueOfTime;
		}
    }

}
