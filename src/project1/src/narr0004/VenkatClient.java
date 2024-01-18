package narr0004;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveToObjectAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.clients.ExampleKnowledge;
import spacesettlers.clients.TeamClient;
import spacesettlers.game.AbstractGameAgent;
import spacesettlers.graphics.*;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.Ship;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

/**
 * Collects nearby asteroids and brings them to the base, picks up beacons as needed for energy.
 *
 * If there is more than one ship, this version happily collects asteroids with as many ships as it
 * has.  it never shoots (it is a pacifist)
 *
 * @author amy
 */

public class VenkatClient extends TeamClient {
    HashMap <UUID, Ship> asteroidToShipMap;
    HashMap <UUID, Boolean> aimingForBase;
    HashMap <UUID, Boolean> justHitBase;
    List<Vertex> allVertices;
    Position currentTarget;
    HashSet<SpacewarGraphics> graphics;
    Graph graph;
    List<Vertex> path;
    Boolean isGoalReached;
    int nextVertex;
    List<Edge> graphEdges;

    List<Vertex> graphVertices;
    int steps;

    Boolean isPresent;

    private static final int NUM_VERTICES = 200;
    private static final int REPLAN = 10;

    /**
     * Example knowledge used to show how to load in/save out to files for learning
     */
    ExampleKnowledge myKnowledge;

    /**
     * Assigns ships to asteroids and beacons, as described above
     */
    public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
                                                      Set<AbstractActionableObject> actionableObjects) {
        HashMap<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();

        // loop through each ship
        for (AbstractObject actionable :  actionableObjects) {
            if (actionable instanceof Ship) {
                Ship ship = (Ship) actionable;

                AbstractAction action;
                action = getAsteroidCollectorAction(space, ship);
                actions.put(ship.getId(), action);

            } else {
                // it is a base.  Heuristically decide when to use the shield (TODO)
                actions.put(actionable.getId(), new DoNothingAction());
            }
        }
        return actions;
    }

    /**
     * Gets the action for the asteroid collecting ship
     * @param space
     * @param ship
     * @return
     */
    private AbstractAction getAsteroidCollectorAction(Toroidal2DPhysics space,
                                                      Ship ship) {
        AbstractAction current = ship.getCurrentAction();
        Position currentPosition = ship.getPosition();

        // aim for a beacon if there isn't enough energy
        if (ship.getEnergy() < 2000) {
            Beacon beacon = pickNearestBeacon(space, ship);
            AbstractAction newAction = null;
            // if there is no beacon, then just skip a turn
            if (beacon == null) {
                newAction = new DoNothingAction();
            } else {
                newAction = new MoveFastAction(space, currentPosition, beacon.getPosition(), beacon.getPosition().getTranslationalVelocity());
            }
            aimingForBase.put(ship.getId(), false);
            return newAction;
        }

        // if the ship has enough resourcesAvailable, take it back to base
        if (ship.getResources().getTotal() > 800) {
            Base base = findNearestBase(space, ship);
            AbstractAction newAction = new MoveFastAction(space, currentPosition, base.getPosition(), base.getPosition().getTranslationalVelocity());
            aimingForBase.put(ship.getId(), true);
            return newAction;
        }

        // otherwise aim for the asteroid
        if (current == null || current.isMovementFinished(space) ||
                (justHitBase.containsKey(ship.getId()) && justHitBase.get(ship.getId()))) {
            justHitBase.put(ship.getId(), false);
            aimingForBase.put(ship.getId(), false);

            AbstractAction newAction = null;

            if (steps >= REPLAN ) {
                currentTarget = null;
                steps = 0;
            }

            //first iteration to find path and traverse
            if (currentTarget == null ) {
                path.clear();
                graphics.clear();
                isGoalReached = false;

                Asteroid asteroid = pickHighestValueNearestFreeAsteroid(space, ship);
                constructGraph(space, ship, currentPosition, asteroid.getPosition(), graph);
                BreadthFirstSearch bfs = new BreadthFirstSearch();
                path = bfs.traverse(allVertices.get(0));

                //if only one vertex needs to be travelled to go to target
                if (currentTarget == null && path.size() == 2) {
                    generateGraphics(space, graph);
                    currentTarget = path.get(nextVertex).getPosition();
                    pathGraphics(ship.getPosition(), currentTarget, space);
                    newAction = new MoveFastAction(space, ship.getPosition(), currentTarget, currentTarget.getTranslationalVelocity());

                    isGoalReached = false;
                    steps = 0;
                }

                else if (path.size() > 2 ) {
                    generateGraphics(space, graph);
                    currentTarget = path.get(nextVertex).getPosition();
                    pathGraphics(ship.getPosition(), currentTarget, space);
                    newAction = new MoveFastAction(space, ship.getPosition(), currentTarget, currentTarget.getTranslationalVelocity());
                    nextVertex = nextVertex + 1;
                    steps = steps + 1;
                }
            }
            // once path found, travel
            else if (currentTarget != null){

                // target not yet reached
                if( isGoalReached == false) {
                    Vertex targetLocation = null;
                    targetLocation = path.get(nextVertex);
                    currentTarget = path.get(nextVertex).getPosition();
                    pathGraphics(ship.getPosition(), currentTarget, space);
                    newAction = new MoveFastAction(space, ship.getPosition(), currentTarget, currentTarget.getTranslationalVelocity());
                    nextVertex = nextVertex + 1;
                    steps = steps + 1;
                    if (targetNotPresent(path.get(path.size() - 1).getPosition(),space)) {
                        currentTarget = null;
                        steps = 0;
                        nextVertex = 1;
                    }
                    if(targetLocation.getIsGoal()) {
                        nextVertex = 1;
                        path.clear();
                        isGoalReached = true;
                    }
                }

                //target reached
                else if(isGoalReached == true ) {
                    currentTarget = null;
                    isGoalReached = true;
                    steps = 0;
                }

                else {
                    newAction = new MoveFastAction(space, ship.getPosition(), currentTarget, currentTarget.getTranslationalVelocity());
                }
            }
            return newAction;
        }
        return ship.getCurrentAction();
    }
    /**
     * Gets the boolean information about a location
     * @param position
     * @param space
     * @return boolean,
     */
    private Boolean targetNotPresent(Position position, Toroidal2DPhysics space) {
        return space.isLocationFree(position, 10);
    }

    /**
     * arrange graphics for the path
     * @param shipPosition
     * @param targetPosition
     * @param space
     * @return
     */
    private void pathGraphics(Position shipPosition, Position targetPosition, Toroidal2DPhysics space) {
        LineGraphics lineGraphics = new LineGraphics(shipPosition, targetPosition, space.findShortestDistanceVector(shipPosition, targetPosition));
        lineGraphics.setLineColor(Color.white);
        lineGraphics.setStrokeWidth(8);
        graphics.add(lineGraphics);
    }
    /**
     * Construct a graph by taking random vertices and taking the goal in space
     * @param space
     * @param ship
     * @param currentPosition
     * @param goalPosition
     * @return
     */
    private void constructGraph(Toroidal2DPhysics space, Ship ship, Position currentPosition, Position goalPosition, Graph graph) {
        allVertices = generateRandomVertices(space);
        allVertices = setShipAndGoalVertex(currentPosition, goalPosition);
        generateEdges(space, ship);
    }

    /**
     * Generates random vertices in the space
     * @param space

     * @return randomVertices
     */
    public List<Vertex> generateRandomVertices(Toroidal2DPhysics space) {
        List<Vertex> randomVertices = new ArrayList<>();
        for (int i = 0; i < NUM_VERTICES; i++) {
            randomVertices.add(randomVertex(space));
        }
        return randomVertices;
    }
    /**
     * It creates a vertex in space
     * @param space

     * @return vertex
     */
    private Vertex randomVertex(Toroidal2DPhysics space) {
        Position position = space.getRandomFreeLocation(random, 15);

        Vertex vertex = new Vertex();
        vertex.setPosition(position);
        vertex.setIsStart(false);
        vertex.setVisited(false);
        vertex.setIsGoal(false);
        vertex.setEdgesList(null);
        return vertex;
    }
    /**
     * It creates a Ship vertex as a start and a goal vertex of nearest asteroid and return all vertices
     * @param shipPosition
     * @param goalPosition

     * @return allVertices
     */
    private List<Vertex> setShipAndGoalVertex(Position shipPosition, Position goalPosition) {

        // ship vertex
        Vertex shipVertex = new Vertex();
        shipVertex.setPosition(shipPosition);
        shipVertex.setIsStart(Boolean.TRUE);
        shipVertex.setIsGoal(Boolean.FALSE);
        shipVertex.setVisited(Boolean.FALSE);
        shipVertex.setEdgesList(null);

        // goal vertex
        Vertex goalVertex = new Vertex();
        goalVertex.setPosition(goalPosition);
        goalVertex.setIsStart(Boolean.FALSE);
        goalVertex.setIsGoal(Boolean.TRUE);
        goalVertex.setVisited(Boolean.FALSE);
        goalVertex.setEdgesList(null);

        //add ship and goal vertices, first is ship and last is goal
        allVertices.add(0, shipVertex);
        allVertices.add(allVertices.size(), goalVertex);

        return allVertices;
    }
    /**
     *  It genrates a edge between two vertices
     * @param space
     * @param ship

     * @return
     */
    private void generateEdges(Toroidal2DPhysics space, Ship ship){
        if (null != allVertices) {
            graph = new Graph();
            graphEdges = new ArrayList<>();
            graphVertices = new ArrayList<>();
            Set<AbstractObject> obstructions = getObstructions(space);

            for (int vertex1 = 0; vertex1 < allVertices.size(); vertex1++) {
                List<Edge> vertexEdges = new ArrayList<>();
                for (int vertex2 = 0 ; vertex2 < allVertices.size(); vertex2++) {
                    double distance = space.findShortestDistance(allVertices.get(vertex1).getPosition(), allVertices.get(vertex2).getPosition());
                    if (distance > 0 && space.isPathClearOfObstructions(allVertices.get(vertex1).getPosition(),
                            allVertices.get(vertex2).getPosition(), obstructions, ship.getRadius() + 10) && distance < 100) {
                        Edge edge = new Edge();
                        edge.setVertex1(allVertices.get(vertex1));
                        edge.setVertex2(allVertices.get(vertex2));
                        if (null != edge) {
                            vertexEdges.add(edge);
                            graphEdges.add(edge);
                        }
                    }
                    else {
                        continue;
                    }
                }
                allVertices.get(vertex1).setEdgesList(vertexEdges);
                graphVertices.add(allVertices.get(vertex1));
            }
            graph.setAllEdges(graphEdges);
            graph.setAllVertices(graphVertices);
        }
    }
    /**
     * It checks if there is anything between two edges.
     * @param space
     * @return allObstructions
     */
    private Set<AbstractObject> getObstructions(Toroidal2DPhysics space){
        Set<AbstractObject> allObstructions =  new HashSet<>();
        Set<AbstractObject> allObjects = space.getAllObjects();

        for (AbstractObject object : allObjects) {
            if(object instanceof Asteroid) {
                if(!((Asteroid) object).isMineable()) {
                    allObstructions.add(object);
                }
            }
            if(object instanceof Base) {
                allObstructions.add(object);
            }
        }

        return allObstructions;
    }
    /**
     * It genrates graphics for all the edges, start and goal positions of the graph.
     * @param space
     * @param graph
     * @return
     */
    private void generateGraphics(Toroidal2DPhysics space, Graph graph) {
        SpacewarGraphics currentGraphics = null;

        for(Vertex vertex: graph.getAllVertices()) {
            if(vertex.getIsStart()) {
                RectangleGraphics rectangleGraphics = new RectangleGraphics(20, Color.GREEN, vertex.getPosition());
                currentGraphics = rectangleGraphics;
            }
            else if(vertex.getIsGoal()) {
                TargetGraphics targetGraphics = new TargetGraphics(20, Color.WHITE, vertex.getPosition());
                currentGraphics = targetGraphics;
            }
            else {
                CircleGraphics circleGraphics = new CircleGraphics(5, Color.YELLOW, vertex.getPosition());
                currentGraphics = circleGraphics;
            }
            graphics.add(currentGraphics);
        }

        for(Edge edge: graph.getAllEdges()) {
            LineGraphics edgeLineGraphics = new LineGraphics(edge.getVertex1().getPosition(), edge.getVertex2().getPosition(), space.findShortestDistanceVector(edge.getVertex1().getPosition(), edge.getVertex2().getPosition()));
            edgeLineGraphics.setLineColor(Color.RED);
            graphics.add(edgeLineGraphics);
        }

    }


    /**
     * Find the base for this team nearest to this ship
     *
     * @param space
     * @param ship
     * @return
     */
    private Base findNearestBase(Toroidal2DPhysics space, Ship ship) {
        double minDistance = Double.MAX_VALUE;
        Base nearestBase = null;

        for (Base base : space.getBases()) {
            if (base.getTeamName().equalsIgnoreCase(ship.getTeamName())) {
                double dist = space.findShortestDistance(ship.getPosition(), base.getPosition());
                if (dist < minDistance) {
                    minDistance = dist;
                    nearestBase = base;
                }
            }
        }
        return nearestBase;
    }

    /**
     * Returns the asteroid of highest value that isn't already being chased by this team
     *
     * @return
     */
    private Asteroid pickHighestValueNearestFreeAsteroid(Toroidal2DPhysics space, Ship ship) {
        Set<Asteroid> asteroids = space.getAsteroids();
        int bestMoney = Integer.MIN_VALUE;
        Asteroid bestAsteroid = null;
        double minDistance = Double.MAX_VALUE;

        for (Asteroid asteroid : asteroids) {
            if (!asteroidToShipMap.containsKey(asteroid.getId())) {
                if (asteroid.isMineable() && asteroid.getResources().getTotal() > bestMoney) {
                    double dist = space.findShortestDistance(asteroid.getPosition(), ship.getPosition());
                    if (dist < minDistance) {
                        bestMoney = asteroid.getResources().getTotal();
                        //System.out.println("Considering asteroid " + asteroid.getId() + " as a best one");
                        bestAsteroid = asteroid;
                        minDistance = dist;
                    }
                }
            }
        }
        //System.out.println("Best asteroid has " + bestMoney);
        return bestAsteroid;
    }


    /**
     * Find the nearest beacon to this ship
     * @param space
     * @param ship
     * @return
     */
    private Beacon pickNearestBeacon(Toroidal2DPhysics space, Ship ship) {
        // get the current beacons
        Set<Beacon> beacons = space.getBeacons();

        Beacon closestBeacon = null;
        double bestDistance = Double.POSITIVE_INFINITY;

        for (Beacon beacon : beacons) {
            double dist = space.findShortestDistance(ship.getPosition(), beacon.getPosition());
            if (dist < bestDistance) {
                bestDistance = dist;
                closestBeacon = beacon;
            }
        }

        return closestBeacon;
    }



    @Override
    public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
        ArrayList<Asteroid> finishedAsteroids = new ArrayList<Asteroid>();

        for (UUID asteroidId : asteroidToShipMap.keySet()) {
            Asteroid asteroid = (Asteroid) space.getObjectById(asteroidId);
            if (asteroid != null && (!asteroid.isAlive() || asteroid.isMoveable())) {
                finishedAsteroids.add(asteroid);
                //System.out.println("Removing asteroid from map");
            }
        }

        for (Asteroid asteroid : finishedAsteroids) {
            asteroidToShipMap.remove(asteroid.getId());
        }

        // check to see who bounced off bases
        for (UUID shipId : aimingForBase.keySet()) {
            if (aimingForBase.get(shipId)) {
                Ship ship = (Ship) space.getObjectById(shipId);
                if (ship.getResources().getTotal() == 0 ) {
                    // we hit the base (or died, either way, we are not aiming for base now)
                    //System.out.println("Hit the base and dropped off resources");
                    aimingForBase.put(shipId, false);
                    justHitBase.put(shipId, true);
                }
            }
        }


    }


    @Override
    public void initialize(Toroidal2DPhysics space) {
        asteroidToShipMap = new HashMap<UUID, Ship>();
        aimingForBase = new HashMap<UUID, Boolean>();
        justHitBase = new HashMap<UUID, Boolean>();
        graphics = new HashSet<SpacewarGraphics>();
        currentTarget = null;
        graph = new Graph();
        isGoalReached = Boolean.FALSE;
        nextVertex = 1;
        path = new ArrayList<>();
        allVertices = new ArrayList<>();
        steps = 0;
        isPresent = true;

    }

    /**
     * Demonstrates saving out to the xstream file
     * You can save out other ways too.  This is a human-readable way to examine
     * the knowledge you have learned.
     */
    @Override
    public void shutDown(Toroidal2DPhysics space) {
//		XStream xstream = new XStream();
//		xstream.alias("ExampleKnowledge", ExampleKnowledge.class);
//
//		try {
//			// if you want to compress the file, change FileOuputStream to a GZIPOutputStream
//			xstream.toXML(myKnowledge, new FileOutputStream(new File(knowledgeFile)));
//		} catch (XStreamException e) {
//			// if you get an error, handle it somehow as it means your knowledge didn't save
//			// the error will happen the first time you run
//			myKnowledge = new ExampleKnowledge();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			myKnowledge = new ExampleKnowledge();
//		}
    }

    @Override
    public Set<SpacewarGraphics> getGraphics() {
        HashSet<SpacewarGraphics> newGraphics = new HashSet<SpacewarGraphics>();
        if (currentTarget != null) {
            newGraphics.addAll(graphics);
        }
        return newGraphics;
    }

    @Override
    /**
     * If there is enough resourcesAvailable, buy a base.  Place it by finding a ship that is sufficiently
     * far away from the existing bases
     */
    public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
                                                     Set<AbstractActionableObject> actionableObjects,
                                                     ResourcePile resourcesAvailable,
                                                     PurchaseCosts purchaseCosts) {

        HashMap<UUID, PurchaseTypes> purchases = new HashMap<UUID, PurchaseTypes>();
        double BASE_BUYING_DISTANCE = 200;
        boolean bought_base = false;

        if (purchaseCosts.canAfford(PurchaseTypes.BASE, resourcesAvailable)) {
            for (AbstractActionableObject actionableObject : actionableObjects) {
                if (actionableObject instanceof Ship) {
                    Ship ship = (Ship) actionableObject;
                    Set<Base> bases = space.getBases();

                    // how far away is this ship to a base of my team?
                    boolean buyBase = true;
                    for (Base base : bases) {
                        if (base.getTeamName().equalsIgnoreCase(getTeamName())) {
                            double distance = space.findShortestDistance(ship.getPosition(), base.getPosition());
                            if (distance < BASE_BUYING_DISTANCE) {
                                buyBase = false;
                            }
                        }
                    }
                    if (buyBase) {
                        purchases.put(ship.getId(), PurchaseTypes.BASE);
                        bought_base = true;
                        //System.out.println("Buying a base!!");
                        break;
                    }
                }
            }
        }

        // can I buy a ship?
        if (purchaseCosts.canAfford(PurchaseTypes.SHIP, resourcesAvailable) && bought_base == false) {
            for (AbstractActionableObject actionableObject : actionableObjects) {
                if (actionableObject instanceof Base) {
                    Base base = (Base) actionableObject;

                    purchases.put(base.getId(), PurchaseTypes.SHIP);
                    break;
                }

            }

        }


        return purchases;
    }

    /**
     * The pacifist asteroid collector doesn't use power ups
     * @param space
     * @param actionableObjects
     * @return
     */
    @Override
    public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
                                                           Set<AbstractActionableObject> actionableObjects) {
        HashMap<UUID, SpaceSettlersPowerupEnum> powerUps = new HashMap<UUID, SpaceSettlersPowerupEnum>();


        return powerUps;
    }

    @Override
    public Map<UUID, AbstractGameAgent> getGameSearch(Toroidal2DPhysics space,
                                                      Set<AbstractActionableObject> actionableObjects) {
        // TODO Auto-generated method stub
        return null;
    }

}