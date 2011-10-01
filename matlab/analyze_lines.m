% Analyzes how pixels in an image look like over time.
%
% Args:
%   filename: full path name of the file to read.
%   columnStart (optional): column index of the start of the line to 
%       analyze. Default to be 1.
%   columnEnd (optional): column index of the end of the line to analyze. 
%       Default to be the maximum width of the frame.
function analyze_lines(filename, columnStart, columnEnd, ...
                       columnSampleRate, frameSampleRate)
  M = dlmread(filename);
  % The first column is the frame id.
  frameid = M(:, 1);
  M(:, 1) = [];
  
  nframeCol = size(M, 2);
  
  if nargin < 2
    columnStart = 1;
  end
  
  if nargin < 3 || columnEnd > nframeCol
    columnEnd = nframeCol;
  end
  
  if nargin < 4
    columnSampleRate = 10;
  end
  
  if nargin < 5
    frameSampleRate = 10;
  end
  
  colRange = columnStart: columnSampleRate : columnEnd;
  M = M(1 : frameSampleRate : end, colRange);
  frameid = frameid(1 : frameSampleRate : end, :);
  
  [nrow, ncol] = size(M);
  fprintf('Number of frames: %d\n', nrow);
  
  
  plot3(repmat(colRange, nrow, 1), repmat(frameid, 1, ncol), M);
  axis([colRange(1) colRange(end) frameid(1) frameid(end) 1150 1200]); 
  xlabel('Pixel position');
  ylabel('Frame');
  zlabel('Depth');
end