%
% Args:
%   filename: full path name of the file to read.
function analyze_lines(filename)
  M = dlmread(filename);
  frame_ids = M(:, 1);
  M(:, 1) = [];
  [rown, coln] = size(M);
  x = 1 : coln;
  plot3(repmat(x, rown, 1), M, repmat(frame_ids, 1, coln));
end