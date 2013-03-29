function finalAhmm = trainahmm(param, data)
% Arg
% - data: cell array of training data.

ahmm = createahmm(param);

engine = smoother_engine(jtree_2TBN_inf_engine(ahmm));

finalAhmm = learn_params_dbn_em(engine, data, 'max_iter', param.maxIter);
end