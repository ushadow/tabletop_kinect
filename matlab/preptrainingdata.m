function [data segFrameId stdConFet modelparam] = ...
         preptrainingdata(dirname, modelparam)
% PREPTRAININGDATA prepares the training data into right structure for 
% preprocesssing.
%
% Return
% data: a cell array of cells. Each cell is a cell array of labels and 
% features. The continuous hand features are standardized.
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
      modelparam.nXconFet = header{3};
      modelparam.nG = length(unique(label.data(:, 2)));
      imageWidth = header{5};
      modelparam.imageSize = imageWidth * imageWidth; 
      [data segFrameId stdConFet] = combinelabelfeature(label.data, ...
                                    feature.data, modelparam);
    end
  end
end
end

function [data segFrameId stdConFet] = combinelabelfeature(label, ...
         feature, param)
labelFrameId = label(:, 1);
featureFrameId = feature(:, 1);
[frameids, labelNDX, featureNDX] = intersect(labelFrameId, featureFrameId);
fprintf('number of frames = %d\n', length(frameids));
[segment segFrameId] = createsegment(frameids); % cell array
label = label(labelNDX, 2 : end); % Removes frame ID.
feature = feature(featureNDX, 2 : end); % Each row is a feature vector.

stdConFet = stdfeature(feature(:, 1 : param.nXconFet));
feature(:, 1 : param.nXconFet) = stdConFet;

assert(param.nF == length(unique(label(:, 2))));
assert(checklabel(label), 'Label is not valid.');
assert(size(label, 1) == size(feature, 1));
assert(all(abs(mean(feature(:, 1 : param.nXconFet), 1)) < 1e-9));

data = cell(1, length(segment));
for i = 1 : length(data)
  indices = segment{i};
  T = length(indices);
  data{i} = cell(param.ss, T);
  data{i}([param.G1 param.F1], :) = num2cell(label(indices, 1: 2)');
  featureSeg = feature(indices, :)';
  featureCell = mat2cell(featureSeg, ...
      [param.nXconFet param.imageSize], ones(1, T));
  for t = 1 : T
    data{i}{4, t} = featureCell(:, t);
  end
  assert(size(data{i}{param.G1, 1}) == 1);
  assert(size(data{i}{param.F1, 1}) == 1);
  assert(isempty(data{i}{param.S1, 1}));
  assert(size(data{i}{4, 1}{1}) == param.nXconFet);
  assert(size(data{i}{4, 1}{2}) == param.imageSize);
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
startNDX = 1;
nframe = length(frameid);
for i = 2 : nframe
  if frameid(i) - 1 ~= frameid(i - 1) ||  i == nframe
    if frameid(i) - 1 ~= frameid(i - 1)
      lastNDX = i - 1;
    elseif i == nframe
      lastNDX = i;
    end
    seg{end + 1} = startNDX : lastNDX; %#ok<AGROW>
    segFrameId{end + 1} = frameid(startNDX : lastNDX); %#ok<AGROW>
    startNDX = i;
  end
end

nframe = 0;
for i = 1 : length(seg)
  nframe = nframe + length(seg{i});
end
assert(nframe == length(frameid));
end

function valid = checklabel(label)
% Checks the validity of G, F labeling.
nrows = size(label, 1);
for i = 1 : nrows - 1
  if label(i, 1) ~= label(i + 1, 1)
    valid = label(i, 2) == 2;
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
