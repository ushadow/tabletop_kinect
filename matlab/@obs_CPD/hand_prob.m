function p = hand_prob(example, template, mu, sigma, use_log)
% HAND_PROB Evaluates the probablity of a hand point cloud matches the 
% template hand point cloud.
%
if nargin < 5, use_log = 0; end

P = image2points(example);
Q = image2points(template);

hd = hausdorfflikedist(P, Q, 1);
p = normalpdf(hd, mu, sigma);

if use_log
  p = log(p);
end

end
