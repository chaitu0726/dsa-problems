// https://leetcode.com/problems/container-with-most-water/
class Solution {
    public int maxArea(int[] height) {
        int left = 0;
        int right = height.length - 1;

        int max = 0;
        int min = 0;

        while(left < right){
            min = height[left] < height[right] ? height[left] : height[right];
            max = max > (min * (right - left)) ? max : (min * (right - left));

            if(height[left] < height[right]) left++;
            else right--;
        }

        return max;
    }
}
