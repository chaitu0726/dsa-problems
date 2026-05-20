// https://leetcode.com/problems/number-of-islands/
/**
  I’ll use recursive DFS because this is essentially a connected-components problem in a grid. DFS cleanly consumes one island at a time.

Time complexity is O(m*n) since every cell is visited once.

Space complexity is O(m*n) in the worst case due to recursion stack depth if the entire grid is one large island.

If stack overflow were a concern for huge inputs, I’d switch to iterative DFS or BFS.

*/
class Solution {

    public int numIslands(char[][] grid) {

        int islands = 0;

        for(int r = 0; r < grid.length; r++) {
            for(int c = 0; c < grid[0].length; c++) {

                if(grid[r][c] == '1') {

                    dfs(grid, r, c);

                    islands++;
                }
            }
        }

        return islands;
    }

    private void dfs(char[][] grid, int r, int c) {

        if(r < 0 ||
           c < 0 ||
           r >= grid.length ||
           c >= grid[0].length ||
           grid[r][c] != '1') {
            return;
        }

        grid[r][c] = '0';

        dfs(grid, r + 1, c);
        dfs(grid, r - 1, c);
        dfs(grid, r, c + 1);
        dfs(grid, r, c - 1);
    }
}
