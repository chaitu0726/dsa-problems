//https://leetcode.com/problems/daily-temperatures/description/

class Solution {
    public int[] dailyTemperatures(int[] temperatures) {
        int n =  temperatures.length;
        int[] ans = new int[n];

        Stack<Integer> s = new Stack<>();
        
        for(int i = 0; i < n; i++){
            while(!s.isEmpty() && temperatures[i] > temperatures[s.peek()]){
                int peek = s.pop();
                ans[peek] = i - peek;
            }
            s.push(i);
        }
        return ans;
    }
}
