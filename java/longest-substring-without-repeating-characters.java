//https://leetcode.com/problems/longest-substring-without-repeating-characters/description/
class Solution {
    public int lengthOfLongestSubstring(String s) {
        
      int max = 0;
        int[] pos = new int[256];
        int left = 0;

        for (int right = 0; right < s.length(); right++) {
            char ch = s.charAt(right);

            // Move left only forward
            left = Math.max(left, pos[ch]);

            max = Math.max(max, right - left + 1);

            pos[ch] = right + 1; // store index + 1
        }

        return max;
    }
}
