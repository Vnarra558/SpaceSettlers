//package narr0004;
//import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.tree.TreeNode;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Stack;
//
///**
// * Depth First Search implementation to find path from start to goal vertex
// *
// */
//public class DepthFirstSearch {
//
//    /**
//     * Generate path from start to goal vertex
//     * @param startVertex start vertex in a graph
//     * @return path path from start to goal vertex
//     */
//    public List<Vertex> traverse (Vertex startVertex) {
//        SearchNode startNode = new SearchNode();
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
//            TreeNode[] treeNodes = finalNode.getPath();
//            for (TreeNode treeNode : treeNodes) {
//                SearchNode node = (SearchNode) ((DefaultMutableTreeNode) treeNode).getUserObject();
//                path.add(node.getVertex());
//            }
//
//        }
//        catch (Exception e) {
//            return path;
//        }
//        return path;
//    }
//
//    /**
//     * Traverse each node from start vertex
//     * @param defaultMutableTreeNode
//     * @param vertex
//     */
//    private DefaultMutableTreeNode getEachNodeByDFS (DefaultMutableTreeNode defaultMutableTreeNode, Vertex vertex) {
//        if (vertex.getGoal()) {
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
//                continue;
//            }
//
//            // unvisited
//            if (!nextVertex.getVisited()) {
//                nextVertex.setVisited(Boolean.TRUE);
//                SearchNode node = new SearchNode();
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