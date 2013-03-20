function H = viewhand(data)
% VIEWHAND display hand images for one sequence.
ncol = 20;
image_width = 50;

H = figure('visible', 'off');

n = size(data, 2);
nrow = ceil(n / ncol);
for j = 1 : n
  hand = reshape(data(:, j), image_width, image_width)';
  image = mat2gray(hand);
  subplot(nrow, ncol, j);
  imshow(image);
end
