// https://leetcode.com/problems/longest-consecutive-sequence/
class Solution {
    public int longestConsecutive(int[] nums) {
        Set<Integer> set = new HashSet<>(nums.length);
        for(int a : nums) set.add(a);

        int max = 0;
        for(int a : set){
            if(!set.contains(a - 1)){
                int curr = a;
                int count = 1;

                while(set.contains(curr + 1)){
                    curr++;
                    count++;
                }

                max = Math.max(count,max);
            }
        }

        return max;
    }
}
