// https://leetcode.com/problems/find-peak-element/description/
class Solution {
    public int findPeakElement(int[] nums) {

        int left = 0;
        int right = nums.length - 1;

        while(left < right){
            int mid = left + (right - left) / 2;
            /**
            The intution is wwe don't have to check both side 
            even if one is smaller then it fails the condition and
            go towards higer side because eventually it will give an answer
             */
            if(nums[mid] < nums[mid + 1]){
                left = mid + 1;
            } else {
                right = mid;
            }

        }

        return left;
    }
}
