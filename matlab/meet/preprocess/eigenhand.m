function [data sortedEigVal eigHand handFeature rawFeature] = ...
         eigenhand(data, neigenhand)
% EIGENHAND computes eigenhands and hand features based on the eigenhands.
%
% [eigHand handFeature rawFeature H] = eigenhand(data) 
% 
% data: a cell array and each cell is a matrix wih N rows where N is the 
%        number of pixels. 
% 
% Returns
% data: data with new standardized hand feature. 
% sortedEigVal: a vector of sorted eigenvalues correspoinding to the 
%               eigHand.
% eigHand: a npixel x K matrix where K is the number of eigenvectors chosen.
% handFeature: K x nframe matrix.
% rawFeature: all hand images in column vectors.

npixel = length(data{1}{4, 1}{2});

nframe = 0;
nseq = length(data);
for i = 1 : nseq
  nframe = nframe + length(data{i});
end

rawFeature = zeros(npixel, nframe);
i = 0;
for l = 1 : length(data)
  data1 = data{l};
  T = length(data1);
  for t = 1 : T 
    i = i + 1;
    rawFeature(:, i) = data1{4, t}{2};
  end
end

meanFeature = mean(rawFeature, 2);
meanFeatureRep = repmat(meanFeature, 1, nframe);
Phi = rawFeature - meanFeatureRep;
A = Phi;

% Let u be the eigenhand. We want to find AA' * u = lamda * u, but AA' is a 
% large matrix.

C = A' * A;
[eigMat, eigVal] = eig(C);

eigValVect = diag(eigVal);
[sortedEigVal, eigNDX] = sort(eigValVect, 'descend');
sortedEigMat = eigMat(:, eigNDX(1 : neigenhand)); % nframe x neighenhand
sortedEigVal = sortedEigVal(1 : neigenhand);

eigHand = normc(A * sortedEigMat); % npixel x neigenhand

handFeature = eigHand' * Phi; % neigenhand x nframe
handFeature = standardize(handFeature);

ndx = 0;
for i = 1 : nseq
  for t = 1 : length(data{i})
    ndx = ndx + 1;
    data{i}{4, t}{2} = handFeature(1 : 3, ndx);
  end
end

assert(all(size(eigHand) == [npixel neigenhand]));
assert(all(size(handFeature) == [neigenhand nframe]));
assert(all(size(data{end}{4, end}{2}) == [3 1]));
assert(all(data{end}{4, end}{2} == handFeature(1 : 3, end)));
assert(abs(norm(eigHand(:, 1)) - 1) < 1e-9);
end
  
