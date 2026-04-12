// https://leetcode.com/problems/permutation-in-string/
class Solution {
    public boolean checkInclusion(String s1, String s2) {
        if(s2.length() < s1.length()) return false;

        int[] freq = new int[26];
        int[] window = new int[26];

        for(int i = 0; i < s1.length(); i++){
            freq[s1.charAt(i) - 'a']++;
            window[s2.charAt(i) - 'a']++;
        }

        int matches = 0;
        for(int  i = 0; i < 26; i++){
            if(freq[i] == window[i]) matches++;
        }
        int left = 0;

        for(int right = s1.length(); right < s2.length(); right++){
            
            if(matches == 26) return true;
            
            int r = s2.charAt(right) - 'a';
            window[r]++;
            
            if(window[r] == freq[r]) matches++;
            else if(window[r] ==  freq[r] + 1) matches--;

            int l = s2.charAt(left) - 'a';
            window[l]--;

            if(window[l] == freq[l]) matches++;
            else if(window[l] == freq[l] - 1) matches--;

            left++;

        }

        return matches == 26;
    }
}
