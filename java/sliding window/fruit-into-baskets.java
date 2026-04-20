// https://leetcode.com/problems/fruit-into-baskets/
class Solution {
    public int totalFruit(int[] fruits) {
        int count = 0;
        int []f = new int[]{-1,-1};

        int right = 0; 
        int max = Integer.MIN_VALUE;
        
        while(right < fruits.length){
            if(f[0] == -1){
                f[0] = fruits[right];
            }else if(f[1] == -1 && fruits[right] != f[0]){
                f[1] = fruits[right];
            }

            if(fruits[right] == f[0] || fruits[right] == f[1]){
                count++;
            }else{
                max = Math.max(max,count);
                count = 1;
                int k = right - 1;
                int t = fruits[k];
                while(k >=0 && fruits[k] == t){
                    k--;
                    count++;
                }
                f[0] = t;
                f[1] = fruits[right];
            }
            right++;
        }

        return Math.max(max,count);
    }
}
