function [data segFrameId modelparam] = preptrainingdata(dirname, ... 
                                                         modelparam)
% PREPTRAININGDATA prepares the training data into right structure for 
% preprocesssing.

files = dir(dirname);
for i = 1 : length(files)
  file = files(i);
  if ~file.isdir
    name = file.name;
    indices = strfind(name, '.');
    last_index = indices(1);
    basename = name(1 : last_index - 1);
    ext = name(last_index + 1 : end);
    if strcmp(ext, 'glab.csv')
      label = importdata([dirname name], ',', 1);
      feature = importdata([dirname basename '.gfet'], ',', 1);
      header = textscan(feature.textdata{1}, '%s%s%d%s%d', ...
                        'delimiter', ',');
      modelparam.nX = header{3};
      imageWidth = header{5};
      modelparam.handSize = imageWidth * imageWidth; 
      [data segFrameId] = combinelabelfeature(label.data, feature.data, ...
                                           modelparam);
    end
  end
end
end

function [data segFrameId] = combinelabelfeature(label, feature, ...
                                                 modelparam)
labelFrameId = label(:, 1);
featureFrameId = feature(:, 1);
[frameids, labelNDX, featureNDX] = intersect(labelFrameId, featureFrameId);
fprintf('number of frames = %d', length(frameids));
[segment segFrameId] = createsegment(frameids); % cell array
label = label(labelNDX, :);
feature = feature(featureNDX, :); % Each row is a feature vector.

standardized = stdfeature(feature(:, 1 : modelparam.nX));
feature(:, 1 : modelparam.nX) = standardized;

assert(modelparam.nG == length(unique(label(:, 2))));
assert(modelparam.nF == length(unique(label(:, 3))));
assert(checklabel(label), 'Label is not valid.');
assert(size(label, 1) == size(feature, 1));
assert(all(abs(mean(feature(:, 1 : modelparam.nX), 1)) < 1e-9));

data = cell(1, length(segment));
for i = 1 : length(data)
  indices = segment{i};
  T = length(indices);
  data{i} = cell(modelparam.ss, T);
  data{i}(1 : 2, :) = num2cell(label(indices, 2 : 3)');
  featureSeg = feature(indices, 2 : end)';
  featureCell = mat2cell(featureSeg, ...
      [modelparam.nX modelparam.handSize], ones(1, T));
  for t = 1 : T
    data{i}{4, t} = featureCell(:, t);
  end
  assert(size(data{i}{1, 1}) == 1);
  assert(size(data{i}{2, 1}) == 1);
  assert(isempty(data{i}{3, 1}));
  assert(size(data{i}{4, 1}{1}) == modelparam.nX);
  assert(size(data{i}{4, 1}{2}) == modelparam.handSize);
end
end

function [seg segFrameId] = createsegment(frameid)
% segments = segment(frameids) finds the segments in a vector of frame 
% IDs. A segment is a sequence of continuous frame IDs. 
% 
% Args
% frameids: a vector of frame IDs.
%
% Returns
% A cell array of indices vectors. Each vector is a continuous indices into
% the input frame ID vector.
seg = {};
segFrameId = {};
start = 1;
for i = 2 : length(frameid)
  if frameid(i) - 1 ~= frameid(i - 1)
    seg{end + 1} = start : i - 1; %#ok<AGROW>
    segFrameId{end + 1} = frameid(start : i - 1); %#ok<AGROW>
    start = i;
  end
end
end

function valid = checklabel(label)
% Checks the validity of G, F labeling.
nrows = size(label, 1);
for i = 1 : nrows - 1
  if label(i, 2) ~= label(i + 1, 2)
    valid = label(i, 3) == 2;
    if ~valid
      disp(label(i, :));
      disp(label(i + 1, :));
      return;
    end
  end
end
end

function transformed = stdfeature(feature)
% stdfeature(feature) Standardizes the features.
%
% Args
% feature: each row is a feature.
  transformed = standardize(feature'); % Changes to column feature.
  transformed = transformed';
end
