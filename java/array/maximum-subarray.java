// https://leetcode.com/problems/maximum-subarray/description/
class Solution {
    public int maxSubArray(int[] nums) {
        int sum = 0;
        int maxSum = Integer.MIN_VALUE;

        for(int num : nums){
            sum += num;
            maxSum = sum > maxSum ? sum : maxSum;
            sum = sum > 0 ? sum : 0;
        }

        return maxSum;
    }
}
