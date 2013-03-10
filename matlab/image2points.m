function points = image2points(image)
% IMAGE2POINTS converts a depth image into points. The image is stored as
% a column vector where pixels in the same row are contiguous.
%
% points = image2points(image)
n = sqrt(length(image));

points = zeros(n * n, 3);

points(:, 1) = repmat((0 : n - 1)', [n 1]); % x
points(:, 2) = reshape(repmat(0 : n - 1, [n 1]), [n * n 1]);
points(:, 3) = image;

index = points(:, 3) ~= 0;
points = points(index, :);
end