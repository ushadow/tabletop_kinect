function [ I ] = get_nfold_indices( num_samples, nfold, seed, k )
% [ I ] = get_nfold_indices( num_samples, nfold )
% Generates nfold indices by random permutation
%
% Input:
%  - num_samples: total number of samples
%  - nfold: desired fold
%  - seed: random seed for randperm (default 0)
%  - k (split mode, default 1)
%      0: Creates [train - test] split
%      1: Creates [train - valid - test] split
%
% Output:
% - I: k-by-nfold cell array. Each column contain indices for a set of k-splits

    if ~exist('seed','var'), seed = 0; end
    if ~exist('k','var'), k = 1; end

    RandStream.setGlobalStream(RandStream('mt19937ar','seed',seed));
    rand_idx = randperm(num_samples);
    fold_size = round(num_samples/nfold);
    if k==0
        I = cell(2,nfold);
        for i=1:nfold
            I{2,i} = rand_idx( (i-1)*fold_size+1 : i*fold_size );
            I{1,i} = setdiff(rand_idx, I{2,i});
        end
    else
        I = cell(3,nfold);    
        for i=1:nfold
            I{2,i} = rand_idx( (i-1)*fold_size+1 : i*fold_size );
            I{3,i} = rand_idx( mod(i,nfold)*fold_size+1 : (mod(i,nfold)+1)*fold_size );
            I{1,i} = setdiff(rand_idx, [I{2,i} I{3,i}]);
        end
    end
end

