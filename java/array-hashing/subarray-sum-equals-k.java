
//https://leetcode.com/problems/subarray-sum-equals-k/
class Solution {
    public int subarraySum(int[] nums, int k) {
       
        Map<Integer,Integer> map = new HashMap<>();
        
        map.put(0,1);
        int count = 0;
        int sum = 0;

        for(int a : nums){
            sum += a;
            if(map.containsKey(sum - k)){
                count += (map.get(sum - k));
            }

            map.put(sum,map.getOrDefault(sum,0) + 1);
        }

       return count; 
    }
}
