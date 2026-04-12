// https://leetcode.com/problems/find-all-anagrams-in-a-string/
class Solution {
    public List<Integer> findAnagrams(String s, String p) {

        if(s.length() < p.length()) return Collections.emptyList();
        
        int[] freq = new int[26];
        int[] window = new int[26];

        for(int i = 0; i < p.length(); i++){
            freq[p.charAt(i) - 'a']++;
            window[s.charAt(i) - 'a']++;
        }

        int matches = 0;
        for(int  i = 0; i < 26; i++){
            if(freq[i] == window[i]) matches++;
        }
        int left = 0;
        List<Integer> ans = new ArrayList<>();

        for(int right = p.length(); right < s.length(); right++){
            
            if(matches == 26){
                ans.add(left);
            }
            
            int r = s.charAt(right) - 'a';
            window[r]++;
            
            if(window[r] == freq[r]) matches++;
            else if(window[r] ==  freq[r] + 1) matches--;

            int l = s.charAt(left) - 'a';
            window[l]--;

            if(window[l] == freq[l]) matches++;
            else if(window[l] == freq[l] - 1) matches--;

            left++;

        }

        if(matches == 26)ans.add(left);

        return ans;
    }
}
