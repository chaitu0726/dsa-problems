//https://leetcode.com/problems/longest-repeating-character-replacement/

class Solution {
    public int characterReplacement(String s, int k) {
        
        int[] freq = new int[26];
        int left = 0;
        int maxFreq = 0;
        int maxLen = 0;

        for (int right = 0; right < s.length(); right++) {
            char ch = s.charAt(right);

            // Step 1: increase frequency
            freq[ch - 'A']++;

            // Step 2: track max frequency in window
            maxFreq = Math.max(maxFreq, freq[ch - 'A']);

            // Step 3: check if window is invalid
            if ((right - left + 1) - maxFreq > k) {
                freq[s.charAt(left) - 'A']--;
                left++;
            }

            // Step 4: update answer
            maxLen = Math.max(maxLen, right - left + 1);
        }

        return maxLen;
     }
}
