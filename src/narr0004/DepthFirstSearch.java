package narr0004;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class DepthFirstSearch {

    /**
     * Generate path from start to goal vertex.
     * @param startVertex start vertex in a graph.
     * @return path from start to goal vertex.
     */
    public List<Vertex> traverse(Vertex startVertex) {
        Set<Vertex> visited = new HashSet<>();
        Stack<DefaultMutableTreeNode> stack = new Stack<>();

        DefaultMutableTreeNode startNode = new DefaultMutableTreeNode(startVertex);
        stack.push(startNode);
        visited.add(startVertex);

        while (!stack.isEmpty()) {
            DefaultMutableTreeNode currentNode = stack.pop();
            Vertex currentVertex = (Vertex) currentNode.getUserObject();

            if (currentVertex.getIsGoal()) {
                return constructPath(currentNode);
            }

            for (Edge edge : currentVertex.getEdgesList()) {
                Vertex adjacentVertex = getAdjacentVertex(edge, currentVertex);

                if (adjacentVertex != null && !visited.contains(adjacentVertex)) {
                    DefaultMutableTreeNode adjacentNode = new DefaultMutableTreeNode(adjacentVertex);
                    currentNode.add(adjacentNode); // Create the tree structure
                    stack.push(adjacentNode);
                    visited.add(adjacentVertex);
                }
            }
        }

        return new ArrayList<>(); // Return an empty list if goal not found.
    }

    /**
     * Get the adjacent vertex from an edge.
     * @param edge The edge to examine.
     * @param currentVertex The current vertex.
     * @return The adjacent vertex.
     */
    private Vertex getAdjacentVertex(Edge edge, Vertex currentVertex) {
        if (edge.getVertex1().equals(currentVertex)) {
            return edge.getVertex2();
        } else if (edge.getVertex2().equals(currentVertex)) {
            return edge.getVertex1();
        }
        return null;
    }

    /**
     * Construct path using the tree nodes.
     * @param goalNode The final goal node.
     * @return List of vertices representing the path.
     */
    private List<Vertex> constructPath(DefaultMutableTreeNode goalNode) {
        List<Vertex> path = new ArrayList<>();
        TreeNode[] treeNodes = goalNode.getPath();
        for (TreeNode treeNode : treeNodes) {
            Vertex vertex = (Vertex) ((DefaultMutableTreeNode) treeNode).getUserObject();
            path.add(vertex);
        }
        return path;
    }
}





//package narr0004;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.Stack;
//
//public class DepthFirstSearch {
//
//    /**
//     * Generate path from start to goal vertex.
//     * @param startVertex start vertex in a graph.
//     * @return path path from start to goal vertex.
//     */
//    public List<Vertex> traverse(Vertex startVertex) {
//        Set<Vertex> visited = new HashSet<>();
//        Stack<Vertex> stack = new Stack<>();
//
//        stack.push(startVertex);
//        visited.add(startVertex);
//
//        List<Vertex> path = new ArrayList<>();
//        while (!stack.isEmpty()) {
//            Vertex currentVertex = stack.pop();
//            path.add(currentVertex);
//
//            if (currentVertex.getIsGoal()) {
//                return path;
//            }
//
//            for (Edge edge : currentVertex.getEdgesList()) {
//                Vertex adjacentVertex = getAdjacentVertex(edge, currentVertex);
//
//                if (adjacentVertex != null && !visited.contains(adjacentVertex)) {
//                    stack.push(adjacentVertex);
//                    visited.add(adjacentVertex);
//                }
//            }
//        }
//
//        return new ArrayList<>(); // Return an empty list if goal not found.
//    }
//
//    /**
//     * Get the adjacent vertex from an edge.
//     * @param edge The edge to examine.
//     * @param currentVertex The current vertex.
//     * @return The adjacent vertex.
//     */
//    private Vertex getAdjacentVertex(Edge edge, Vertex currentVertex) {
//        if (edge.getVertex1().equals(currentVertex)) {
//            return edge.getVertex2();
//        } else if (edge.getVertex2().equals(currentVertex)) {
//            return edge.getVertex1();
//        }
//        return null;
//    }
//}
//



//package narr0004;
//
//import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.tree.TreeNode;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Depth First Search implementation to find path from start to goal vertex
// *
// */
//public class DepthFirstSearch {
//
//	/**
//	 * Generate path from start to goal vertex
//	 * @param startVertex start vertex in a graph
//	 * @return path path from start to goal vertex
//	 */
//    public List<Vertex> traverse (Vertex startVertex) {
//        Search startNode = new Search();
//        List<Vertex> path = new ArrayList<>();
//        startVertex.setVisited(Boolean.TRUE);
//        startNode.setVertex(startVertex);
//        startNode.setEdge(null);
//
//        // root
//        DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(startNode);
//        DefaultMutableTreeNode finalNode = getEachNodeByDFS(defaultMutableTreeNode, startVertex);
//
//        try {
//        	TreeNode[] treeNodes = finalNode.getPath();
//        	for (TreeNode treeNode : treeNodes) {
//                Search node = (Search) ((DefaultMutableTreeNode) treeNode).getUserObject();
//                path.add(node.getVertex());
//         }
//
//        }
//        catch (Exception e) {
//			return path;
//		}
//        return path;
//    }
//
//    /**
//	 * Traverse each node from start vertex
//	 * @param defaultMutableTreeNode
//	 * @param vertex
//	 */
//    private DefaultMutableTreeNode getEachNodeByDFS (DefaultMutableTreeNode defaultMutableTreeNode, Vertex vertex) {
//        if (vertex.getIsGoal()) {
//            return defaultMutableTreeNode;
//        }
//
//        for (Edge edge : vertex.getEdgesList()) {
//            Vertex vertex1 = edge.getVertex1();
//            Vertex vertex2 = edge.getVertex2();
//            Vertex nextVertex = new Vertex();
//
//            if (vertex1.getVisited())
//                nextVertex = vertex2;
//            else {
//            	continue;
//            }
//
//            // unvisited
//            if (!nextVertex.getVisited()) {
//                nextVertex.setVisited(Boolean.TRUE);
//                Search node = new Search();
//                node.setVertex(nextVertex);
//                node.setEdge(edge);
//
//                DefaultMutableTreeNode defaultMutableTreeNode1 = new DefaultMutableTreeNode(node);
//                //add child
//                defaultMutableTreeNode.add(defaultMutableTreeNode1);
//                //recursive
//                return getEachNodeByDFS(defaultMutableTreeNode1, nextVertex);
//            }
//        }
//        return null;
//    }
//}
//
