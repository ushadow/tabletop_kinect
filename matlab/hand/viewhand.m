function H = viewhand(data, feature)
% VIEWHAND displays hand images for one sequence.
% 
% H = viewhand(data)
%
% Args
% data: N X M matrix where N is the number of pixels in the image and M is 
%       number of frames.
[N M] = size(data);
imageWidth = sqrt(N);
ncol = floor(1000 / imageWidth);
nrow = ceil(M / ncol);

H = figure('visible', 'off');

for j = 1 : M
  hand = reshape(data(:, j), imageWidth, imageWidth)';
  image = mat2gray(hand);
  subplot(nrow, ncol, j);
  imshow(image);
  title(strjoin(feature(:, j), ','));
end
