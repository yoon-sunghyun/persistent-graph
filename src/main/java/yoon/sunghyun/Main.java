package yoon.sunghyun;

import yoon.sunghyun.adt.graph.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.Objects;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        String[] fileNames = {
          "CollegeMsg.txt",     // https://snap.stanford.edu/data/CollegeMsg.html
          "Email-EuAll.txt",    // https://snap.stanford.edu/data/email-EuAll.html
        };
        String url = "jdbc:mariadb://localhost:3306/";
        String username = "root";
        String password = "root";
        String dbName = "test_db";

        Graph graph = new PersistentGraph(url, username, password, dbName);

        testGraph(graph, fileNames[1], "\\s");

        graph.shutdown();
    }

    @SuppressWarnings("SameParameterValue")
    static void testGraph(Graph graph, String fileName, String delimiter) throws Exception
    {
        URI uri = Objects.requireNonNull(Main.class.getClassLoader().getResource(fileName)).toURI();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(uri)));

        int lineCounter = 0;
        final String propOdd = "ODD";
        final String propAdd = "ADD";
        final String propSub = "SUB";
        final String propDiv = "DIV";

        while (true)
        {
            String line = bufferedReader.readLine();

            if (line == null)
            {
                System.out.println("lines read: " + lineCounter);
                break;
            }
            else if (line.startsWith("#"))
            {
                continue;
            }
            if ((++lineCounter % 1000) == 0)
            {
                System.out.println("lines read: " + lineCounter);
            }

            String[] vertexIDs = line.split(delimiter);
            int vO_ID = Integer.parseInt(vertexIDs[0]);
            int vI_ID = Integer.parseInt(vertexIDs[1]);

            Vertex vertexO = graph.addVertex(Integer.toString(vO_ID));
            Vertex vertexI = graph.addVertex(Integer.toString(vI_ID));
            Edge edge = graph.addEdge(vertexO, vertexI, "label");

            edge.setProperty(propOdd, ((vO_ID + vI_ID) % 2 == 0));
            edge.setProperty(propAdd, (vO_ID + vI_ID));
            edge.setProperty(propSub, (vO_ID - vI_ID));
            edge.setProperty(propDiv, (1.0 * vO_ID / vI_ID));
        }
        bufferedReader.close();
        System.out.println("data loaded");

        // finding the vertices with maximum out-degree and in-degree
        String maxDegO_ID = null;
        String maxDegI_ID = null;
        int maxDegO = Integer.MIN_VALUE;
        int maxDegI = Integer.MIN_VALUE;

        for (Vertex vertex : graph.getVertices())
        {
            int degreeO = vertex.getVertices(Direction.OUT).size();
            if (degreeO > maxDegO)
            {
                maxDegO_ID = vertex.getId();
                maxDegO = degreeO;
            }

            int degreeI = vertex.getVertices(Direction.IN).size();
            if (degreeI > maxDegI)
            {
                maxDegI_ID = vertex.getId();
                maxDegI = degreeI;
            }
        }

        System.out.println("[ 1] " + maxDegO_ID);
        System.out.println("[ 2] " + maxDegO);
        System.out.println("[ 3] " + maxDegI_ID);
        System.out.println("[ 4] " + maxDegI);
        System.out.println("[ 5] " + graph.getVertices().size());
        System.out.println("[ 6] " + graph.getEdges().size());
        System.out.println("[ 7] " + graph.getEdges(propOdd, true).size());
        System.out.println("[ 8] " + graph.getEdges(propOdd, false).size());
        System.out.println("[ 9] " + graph.getVertex(maxDegO_ID).getVertices(Direction.OUT).size());
        System.out.println("[10] " + graph.getVertex(maxDegI_ID).getVertices(Direction.IN).size());

        long tmpTime, minTime, minSize = 0;

        minTime = Long.MAX_VALUE;
        for (int i = 0; i < 10; i++)
        {
            tmpTime = System.nanoTime();
            minSize = graph.getVertex(maxDegO_ID)
                    .getVertices(Direction.OUT).stream()
                    .flatMap(v -> v.getVertices(Direction.OUT).stream())
                    .toList().size();
            tmpTime = System.nanoTime() - tmpTime;
            if (tmpTime < minTime)
            {
                if (minTime != Long.MAX_VALUE)
                    System.out.printf("%10d -> %10d\n", minTime, tmpTime);
                minTime = tmpTime;
            }
        }
        System.out.println("[11] " + minSize);
        System.out.println("[T1] " + minTime);

        minTime = Long.MAX_VALUE;
        for (int i = 0; i < 10; i++)
        {
            tmpTime = System.nanoTime();
            minSize = graph.getVertex(maxDegI_ID)
                    .getVertices(Direction.IN).stream()
                    .flatMap(v -> v.getVertices(Direction.IN).stream())
                    .toList().size();
            tmpTime = System.nanoTime() - tmpTime;
            if (tmpTime < minTime)
            {
                if (minTime != Long.MAX_VALUE)
                    System.out.printf("%10d -> %10d\n", minTime, tmpTime);
                minTime = tmpTime;
            }
        }
        System.out.println("[12] " + minSize);
        System.out.println("[T2] " + minTime);

        minTime = Long.MAX_VALUE;
        for (int i = 0; i < 10; i++)
        {
            tmpTime = System.nanoTime();
            minSize = graph.getVertex(maxDegO_ID)
                    .getVertices(Direction.OUT, propOdd, true).stream()
                    .flatMap(v -> v.getVertices(Direction.OUT, propOdd, false).stream())
                    .toList().size();
            tmpTime = System.nanoTime() - tmpTime;
            if (tmpTime < minTime)
            {
                if (minTime != Long.MAX_VALUE)
                    System.out.printf("%10d -> %10d\n", minTime, tmpTime);
                minTime = tmpTime;
            }
        }
        System.out.println("[13] " + minSize);
        System.out.println("[T3] " + minTime);

        minTime = Long.MAX_VALUE;
        for (int i = 0; i < 10; i++)
        {
            tmpTime = System.nanoTime();
            minSize = graph.getVertex(maxDegI_ID)
                    .getVertices(Direction.IN, propOdd, true).stream()
                    .flatMap(v -> v.getVertices(Direction.IN, propOdd, false).stream())
                    .toList().size();
            tmpTime = System.nanoTime() - tmpTime;
            if (tmpTime < minTime)
            {
                if (minTime != Long.MAX_VALUE)
                    System.out.printf("%10d -> %10d\n", minTime, tmpTime);
                minTime = tmpTime;
            }
        }
        System.out.println("[14] " + minSize);
        System.out.println("[T4] " + minTime);

        minTime = Long.MAX_VALUE;
        for (int i = 0; i < 10; i++)
        {
            tmpTime = System.nanoTime();
            minSize = graph.getVertex(maxDegO_ID)
                    .getTwoHopVertices(Direction.OUT).size();
            tmpTime = System.nanoTime() - tmpTime;
            if (tmpTime < minTime)
            {
                if (minTime != Long.MAX_VALUE)
                    System.out.printf("%10d -> %10d\n", minTime, tmpTime);
                minTime = tmpTime;
            }
        }
        System.out.println("[15] " + minSize);
        System.out.println("[T5] " + minTime);

        minTime = Long.MAX_VALUE;
        for (int i = 0; i < 10; i++)
        {
            tmpTime = System.nanoTime();
            minSize = graph.getVertex(maxDegI_ID)
                    .getTwoHopVertices(Direction.IN).size();
            tmpTime = System.nanoTime() - tmpTime;
            if (tmpTime < minTime)
            {
                if (minTime != Long.MAX_VALUE)
                    System.out.printf("%10d -> %10d\n", minTime, tmpTime);
                minTime = tmpTime;
            }
        }
        System.out.println("[16] " + minSize);
        System.out.println("[T6] " + minTime);
    }
}
