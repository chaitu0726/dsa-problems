// https://leetcode.com/problems/next-greater-element-i/description/
class Solution {
    public int[] nextGreaterElement(int[] nums1, int[] nums2) {
        Map<Integer,Integer> map = new HashMap<>();
        Stack<Integer> stack = new Stack<>();
        /**
            nums1 = [4,1,2], nums2 = [1,3,4,2]
            Output: [-1,3,-1]
         */
        for(int num : nums2){
            while(!stack.isEmpty() && stack.peek() < num){
                map.put(stack.pop(), num);
            }
            stack.push(num);
        }

        while(!stack.isEmpty()){
            map.put(stack.pop(), -1);
        }

        int[] ans = new int[nums1.length];
        for(int i = 0; i < nums1.length; i++){
            ans[i] = map.get(nums1[i]);
        }
        return ans;
    }
}
