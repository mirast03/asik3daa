package dsu;

public class DisjointSet {
    private final int[]parent;
    public DisjointSet(int size){
        parent=new int[size];
        for(int i=0;i<size;i++){
            parent[i]=i;
        }
    }
    public int find (int vertex){
        while (parent[vertex]!=vertex){
            vertex=parent[vertex];
        }
        return vertex;
    }
    public void union(int first,int second){
        int firstRoot=find(first);
        int secondRoot=find(second);
        if(firstRoot==secondRoot){
            return;
        }
        parent[secondRoot]=firstRoot; 
    }
}
