function learnahmmparam(ahmm, data, max_iter)
% LEARN_AHMM_PARAMS
%
% learn_ahmm_params(data)
% ahmm: the model.
% data: training data. A cell-array of 2D cell-arrays. For example,
% case{l}{i, t} is the value of node i in slice t in sequel l, or [] if
% unobserved.

engine = smoother_engine(jtree_2TBN_inf_engine(ahmm));

learn_params_dbn_em(engine, data, 'max_iter', max_iter);
end