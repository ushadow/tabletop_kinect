function analyzefeature(data, segFrameId)
seq = data{1};
frameid = segFrameId{1};
nframe = length(seq);
dist = zeros(1, nframe);
for i = 2 : nframe
  dist(i) = norm(seq{4, i}{1} - seq{4, i - 1}{1}); 
end

figure(1);
plot(frameid, dist, 'b+-');
figure(2);
boxplot(dist);
end