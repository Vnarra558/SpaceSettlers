

package narr0004;
import javax.swing.tree.DefaultMutableTreeNode;
import spacesettlers.simulator.Toroidal2DPhysics;
import java.util.*;
import javax.swing.tree.TreeNode;

public class AstarSearch {

    private Set<Vertex> explored = new HashSet<>();
    private PriorityQueue<Search> fringe =
            new PriorityQueue<>((a, b) -> Double.compare(a.getfCost(), b.getfCost()));

    /**
     * main method to search for path using astar
     * @param startVertex start vertex in a graph
     * @param goalVertex goal vertex in a graph
     * @return path path from start to goal vertex
     */
    public List<Vertex> search(Toroidal2DPhysics space, Vertex startVertex, Vertex goalVertex) {
        List<Vertex> path = new ArrayList<>();

        Search startNode = new Search();
        startNode.setVertex(startVertex);
        startNode.setgCost(0);
        startNode.sethCost(calculateHeuristic(space, startVertex, goalVertex));
        startNode.setfCost(startNode.getgCost() + startNode.gethCost());

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(startNode);
        DefaultMutableTreeNode finalNode = searchByAstar(space, rootNode, startNode, goalVertex);

        //get path
        try {
            TreeNode[] treeNodes = finalNode.getPath();
            for (TreeNode treeNode : treeNodes) {
                Search node = (Search) ((DefaultMutableTreeNode) treeNode).getUserObject();
                path.add(node.getVertex());
            }
        } catch (Exception e) {
            return path;
        }
        return path;
    }

    /**
     * calculate heuristic cost
     * @param space
     * @param v1 frist vertex
     * @param v2 second vertex
     * @return distance
     */
    private double calculateHeuristic(Toroidal2DPhysics space, Vertex v1, Vertex v2) {
        return space.findShortestDistance(v1.getPosition(), v2.getPosition());
    }

    /**
     * a star algorithm implementation
     * @param space
     * @param rootNode
     * @param currentSearch
     * @param goalVertex
     * @return
     */
    private DefaultMutableTreeNode searchByAstar(Toroidal2DPhysics space, DefaultMutableTreeNode rootNode, Search currentSearch, Vertex goalVertex) {

        int maxSteps = 0;

        fringe.add(currentSearch);

        while (true) {

            // fringe is empty or max steps reached
            if (fringe.isEmpty() || maxSteps >= 10) {
                return rootNode;
            }

            currentSearch = fringe.poll();
            Vertex currentVertex = currentSearch.getVertex();
            DefaultMutableTreeNode currentNode = findNode(rootNode, currentVertex);

            // if the current vertex is the goal
            if (currentVertex.getIsGoal()) {
                return currentNode;
            }

            // if the current vertex hasn't been explored yet
            if (!explored.contains(currentVertex)) {
                explored.add(currentVertex);

                for (Edge edge : currentVertex.getEdgesList()) {
                    Vertex childVertex = edge.getVertex2();

                    double gCost = currentSearch.getgCost() + calculateHeuristic(space,currentVertex, childVertex);
                    double hCost = calculateHeuristic(space, childVertex, goalVertex);
                    double fCost = gCost + hCost;

                    Search childSearch = new Search();
                    childSearch.setVertex(childVertex);
                    childSearch.setgCost(gCost);
                    childSearch.sethCost(hCost);
                    childSearch.setfCost(fCost);

                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childSearch);
                    currentNode.add(childNode);

                    if (!explored.contains(childVertex) && !fringeContains(childSearch)) {
                        fringe.add(childSearch);
                    }
                }
            }
            maxSteps++;
        }
    }

    /**
     * to check if the fringe already contains search node.
     */
    private boolean fringeContains(Search s) {
        for (Search fringeSearch : fringe) {
            if (fringeSearch.getVertex().equals(s.getVertex())) {
                if (fringeSearch.getfCost() <= s.getfCost()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * to find a node recursively
     */
    private DefaultMutableTreeNode findNode(DefaultMutableTreeNode rootNode, Vertex v) {
        if (((Search) rootNode.getUserObject()).getVertex().equals(v)) {
            return rootNode;
        }

        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            DefaultMutableTreeNode result = findNode(childNode, v);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
