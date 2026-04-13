// https://leetcode.com/problems/top-k-frequent-elements/
class Solution {
    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer,Integer> map = new HashMap<>();
        for(int a : nums){
            map.put(a, map.getOrDefault(a,0) + 1);
        }
        int[] ans = new int[k];

        List<Integer>[] bucket = new List[nums.length + 1];
        
        for(int key : map.keySet()){
            int freq = map.get(key);
            if(bucket[freq] == null){
                bucket[freq] = new ArrayList<>();
            }
            bucket[freq].add(key);
        }

        // int p = 0;
        int idx = 0;
        for(int i = bucket.length - 1; i >= 0 && idx < k; i--){
            if(bucket[i] != null){
                for(int b : bucket[i]){
                    ans[idx++] = b;
                    if(idx == k)break;
                }
            }
        }
        

        return ans;
    }
}
