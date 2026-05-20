class Solution {
    public int numIslands(char[][] grid) {

        int[][] directions = {
            {1,0},
            {-1,0},
            {0,1},
            {0,-1}
        };
        
        int rows = grid.length;
        int col = grid[0].length;

        boolean[][] visited = new boolean[rows][col];
        int island  = 0;

        for(int i = 0; i < rows; i++){
            for(int j = 0; j < col; j++){
                if(grid[i][j] == '1' && !visited[i][j]){
                    // BFS **
                    visited[i][j] = true;
                    Queue<int[]> queue = new ArrayDeque<>();
                    queue.offer(new int[]{i,j});

                    while(!queue.isEmpty()){
                        int[] cell = queue.poll();

                        int r = cell[0];
                        int c = cell[1];

                        for(int[] dr : directions){
                            int nr = r + dr[0];
                            int nc = c + dr[1];

                            if(  nr >= 0 && nc >= 0
                              && nr < rows && nc < col
                              && grid[nr][nc] == '1'
                              && !visited[nr][nc]
                            ){
                                visited[nr][nc] = true;
                                queue.offer(new int[]{nr,nc});
                            }
                        }
                    }
                    
                    // **
                    island += 1;
                }
            }
        }

        return island;
    }
}
