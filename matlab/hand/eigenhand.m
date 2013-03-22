function [sortedEigVal eigHand handFeature rawFeature H] = ...
         eigenhand(data, varargin)
% EIGENHAND computes eigenhands and hand features based on the eigenhands.
%
% [eigHand handFeature rawFeature H] = eigenhand(data) 
% 
% data: a cell array and each cell is a matrix wih N rows where N is the 
%        number of pixels. 
% 
% Returns
% sortedEigVal: a vector of sorted eigenvalues correspoinding to the 
%               eigHand.
% eigHand: a N x K matrix where K is the number of eigenvectors chosen.
% handFeature: K x M matrix where M is total number of frames in the data 
%              set.
% rawFeature: all hand images in column vectors.
%
% Optional arguments
% 'plot': if 1, plots the eigenhands.

plot = 0;
args = varargin;
for i = 1 : 2 : length(args)
  switch args{i},
    case 'plot', plot = args{1 + 1};
  end
end

K = 100;
nrow = 10;
ncol = 20;
N = length(data{1}{4, 1}{2});
imageWidth = sqrt(N);

M = 0;
for i = 1 : length(data)
  M = M + length(data{i});
end

rawFeature = zeros(N, M);
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
meanFeatureRep = repmat(meanFeature, 1, M);
Phi = rawFeature - meanFeatureRep;
A = Phi;

% Let u be the eigenhand. We want to find AA' * u = lamda * u, but AA' is a 
% large matrix.

C = A' * A;
[eigMat, eigVal] = eig(C);

eigValVect = diag(eigVal);
[sortedEigVal, eigNDX] = sort(eigValVect, 'descend');
sortedEigMat = eigMat(:, eigNDX(1 : K)); % nframe x K
sortedEigVal = sortedEigVal(1 : K);

eigHand = normc(A * sortedEigMat); % n x K

close all;

% Plots eigenhands.
if plot
  H = figure;
  for i = 1 : K
    eigHand1 = reshape(eigHand(:, i), imageWidth, imageWidth)';
    image = mat2gray(eigHand1);
    subplot(nrow, ncol, i);
    imshow(image);
  end
end
handFeature = eigHand' * Phi;

assert(all(size(eigHand) == [N K]));
assert(all(size(handFeature) == [K, M]));
assert(abs(norm(eigHand(:, 1)) - 1) < 1e-9);
end
  
