function points = image2points(image)
% IMAGE2POINTS converts a 2D depth image into points.
%
% points = image2points(image)
n = size(image, 1);

points = zeros(n * n, 3);

points(:, 1) = repmat((1 : n)', [n 1]);
points(:, 2) = reshape(repmat(1 : n, [n 1]), [n * n 1]);
points(:, 3) = reshape(image, [n * n 1]);

index = points(:, 3) ~= 0;
points = points(index, :);
end