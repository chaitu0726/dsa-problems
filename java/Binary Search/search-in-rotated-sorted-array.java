// https://leetcode.com/problems/search-in-rotated-sorted-array/
class Solution {
    public int search(int[] nums, int target) {
        int start = 0, end = nums.length - 1;
        /**
            [4,5,6,-1,0,1,2] ,0
            The intution is when you divide the rotated sorted array at any mid point 
            either left or right must be sorted.
            so we will check left or right side whether it is in between range and accordingly move the start/end
        */
        while(start <= end){
            int mid = start + (end - start) / 2;

            if(nums[mid] == target) return mid;

            // Left half sorted
            if(nums[start] <= nums[mid]){
                if(target >= nums[start] && target < nums[mid]){
                    end = mid - 1;
                } else {
                    start = mid + 1;
                }
            } 
            // Right half sorted
            else {
                if(target > nums[mid] && target <= nums[end]){
                    start = mid + 1;
                } else {
                    end = mid - 1;
                }
            }
        }

        return -1;
    }
}
