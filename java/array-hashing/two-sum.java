// https://leetcode.com/problems/two-sum/
class Solution {
    public int[] twoSum(int[] nums, int target) {
        int []a = new int[2];
        Map<Integer,Integer> v = new HashMap<Integer,Integer>(nums.length);
            for(int i=0;i<nums.length;i++){
                int t = target - nums[i];
                if(v.containsKey(t)){
                    a[0] = v.get(t);
                    a[1] = i;
                   return a;
                }
                v.put(nums[i],i);
            }
        return new int[]{-1,-1};
    }
}
