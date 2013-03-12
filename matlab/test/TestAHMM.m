classdef TestAHMM < TestCase
  
properties
  params;
  hand_size = 4 * 4;
end

methods
  function self = TestAHMM(name)
    self = self@TestCase(name);
  end
    
  function setUp(self)
    self.params.nG = 4;
    self.params.nS = 4;
    self.params.nF = 2;
    self.params.nX = 2;
    
    % Node label
    self.params.G1 = 1; 
    self.params.S1 = 2; 
    self.params.F1 = 3; 
    self.params.X1 = 4; 

    self.params.onodes = [self.params.X1];

    self.params.Gstartprob = [1 0 0 0];
    self.params.Gtransprob = [0 1 0 0
                              0 0 1 0
                              0 0 0 1
                              1 0 0 0];

    % 2 x 4 matrix                   
    self.params.Sstartprob = [1 0 0 0
                              0 1 0 0
                              0 0 1 0
                              0 0 0 1];
                       
    self.params.Stransprob = zeros(self.params.nS, self.params.nG, ...
                                   self.params.nS);
    self.params.Stransprob(:, 1, :) = [1 0 0 0 
                                       1 0 0 0 
                                       1 0 0 0 
                                       1 0 0 0];
    self.params.Stransprob(:, 2, :) = [0 1 0 0 
                                       0 1 0 0
                                       0 1 0 0
                                       0 1 0 0];

    self.params.Stransprob(:, 3, :) = [0 0 1 0 
                                       0 0 1 0
                                       0 0 1 0
                                       0 0 1 0];
                                
    self.params.Stransprob(:, 4, :) = [0 0 0 1 
                                       0 0 0 1
                                       0 0 0 1
                                       0 0 0 1];

    self.params.Stermprob = ones(self.params.nG, self.params.nS, 2);
    self.params.Stermprob(1, :, :) = [0 1
                                      0 0
                                      0 0
                                      0 0];
    self.params.Stermprob(2, :, :) = [0 0
                                      0.4 0.6
                                      0 0
                                      0 0];
    self.params.Stermprob(3, :, :) = [0 0
                                      0 0
                                      0 1
                                      0 0];
    self.params.Stermprob(4, :, :) = [0 0
                                      0 0
                                      0 0
                                      0 1];
                             
    self.params.hand = zeros(self.hand_size, self.params.nS);
    for i = 1 : self.params.nS
      self.params.hand(:, i) = repmat(i, self.hand_size, 1);
    end

    self.params.hd_mu = 0;
    self.params.hd_sigma = 1;

    self.params.Xmean = reshape(1 : self.params.nX * self.params.nS, ...
                                [self.params.nX self.params.nS]);
    self.params.Xcov = repmat(eye(self.params.nX) * 0.0001, ...
                              [1, 1, self.params.nS]);
  end

  function testSample(self)
    ahmm = createmodel(self.params);
    ns = ahmm.node_sizes_slice;
    assertTrue(all(ns == [4 4 2 2]));
    
    T = 10;
    evidence = sample_dbn(ahmm, T);

    G1 = 1; S1 = 2; F1 = 3; X1 = 4; 
    for i = 1 : T
      s = evidence{S1, i};
      hand = evidence{X1, i}{2};
      assertTrue(all(size(hand) == [self.hand_size, 1]));
      assertTrue(all(size(evidence{X1, i}{1}) == [ns(X1), 1]));
      assertTrue(all(hand(:) == s));
      assertTrue(s == evidence{G1, i});
      if i > 1 
        f = evidence{F1, i - 1};
        if f == 1
          assertTrue(evidence{G1, i - 1} == evidence{G1, i})
        else
          assertTrue(f == 2);
        end
      end
    end
  end
  
  function testInference(self)
    det_params = self.deterministicParams;
    ahmm = createmodel(det_params);
    engine = smoother_engine(jtree_2TBN_inf_engine(ahmm));

    T = 4;
    ss = length(ahmm.intra);
    assertTrue(ss == 4);
    onodes = ahmm.observed;
    assertTrue(onodes == 4);

    maxIter = 10;
    for i = 1 : maxIter
      ev = sample_dbn(ahmm, 'length', T);
      %logdebug('TestAHMM', 'ev', ev);
      evidence = cell(ss, T);
      evidence(onodes, :) = ev(onodes, :);

      engine = enter_evidence(engine, evidence);
      hnodes = mysetdiff(1 : ss, onodes);
      map = TestAHMM.getMarginals(engine, hnodes, T);
      %logdebug('TestAHMM', 'map', map);
      for t = 1 : T
        for n = hnodes(:)'
          assertTrue(map(n, t) == ev{n, t});
        end
      end
    end
  end
  
  function testLearning(self)
    T = 10;
    max_iter = 10;
    true_params = self.deterministicParams;
    ahmm = createmodel(true_params);
    ev = sample_dbn(ahmm, 'length', T);
    ss = length(ahmm.intra);
    evidence = cell(1, 1);
    evidence{1} = cell(ss, T);
   
    new_params = self.priorParams;
    evidence{1}(new_params.onodes, :) = ev(new_params.onodes, :);
    
    new_ahmm = createmodel(new_params);
    engine = smoother_engine(jtree_2TBN_inf_engine(new_ahmm));
    final_ahmm = learn_params_dbn_em(engine, evidence, ...
        'max_iter', max_iter);
    
    learned_Gstartprob = CPD_to_CPT(final_ahmm.CPD{1});
    assertTrue(all(learned_Gstartprob == true_params.Gstartprob(:)));
    learned_Sstartprob = CPD_to_CPT(final_ahmm.CPD{2});
    assertTrue(learned_Sstartprob(1, 1) == 1);
    learned_Stermprob = CPD_to_CPT(final_ahmm.CPD{3});
    assertTrue(all(learned_Stermprob(:) == true_params.Stermprob(:)));
    learned_hand = struct(final_ahmm.CPD{4}).hand;
    assertTrue(all(learned_hand(:) == true_params.hand(:)));
  end
  
  function params = deterministicParams(self)
    params.nG = 4;
    params.nS = 4;
    params.nF = 2;
    params.nX = 2;
    
    % Node label
    params.G1 = 1; 
    params.S1 = 2; 
    params.F1 = 3; 
    params.X1 = 4; 

    params.onodes = [params.X1];

    params.Gstartprob = [1 0 0 0];
    params.Gtransprob = [0 1 0 0
                         0 0 1 0
                         0 0 0 1
                         1 0 0 0];

    % 2 x 4 matrix                   
    params.Sstartprob = [1 0 0 0
                              0 1 0 0
                              0 0 1 0
                              0 0 0 1];
                       
    params.Stransprob = zeros(params.nS, params.nG, params.nS);
    params.Stransprob(:, 1, :) = [1 0 0 0 
                                  1 0 0 0 
                                  1 0 0 0 
                                  1 0 0 0];
    params.Stransprob(:, 2, :) = [0 1 0 0 
                                  0 1 0 0
                                  0 1 0 0
                                  0 1 0 0];

    params.Stransprob(:, 3, :) = [0 0 1 0 
                                  0 0 1 0
                                  0 0 1 0
                                  0 0 1 0];
                                
    params.Stransprob(:, 4, :) = [0 0 0 1 
                                  0 0 0 1
                                  0 0 0 1
                                  0 0 0 1];

    params.Stermprob = ones(params.nG, params.nS, 2);
    params.Stermprob(1, :, :) = [0 1
                                 0 0
                                 0 0
                                 0 0];
    params.Stermprob(2, :, :) = [0 0
                                 0 1
                                 0 0
                                 0 0];
    params.Stermprob(3, :, :) = [0 0
                                 0 0
                                 0 1
                                 0 0];
    params.Stermprob(4, :, :) = [0 0
                                 0 0
                                 0 0
                                 0 1];
                             
    params.hand = zeros(self.hand_size, params.nS);
    for i = 1 : params.nS
      params.hand(:, i) = repmat(i, self.hand_size, 1);
    end

    params.hd_mu = 0;
    params.hd_sigma = 1;

    params.Xmean = reshape(1 : params.nX * params.nS, ...
                           [params.nX params.nS]);
    params.Xcov = repmat(eye(params.nX) * 0.1, [1, 1, params.nS]);
  end
  
  function params = priorParams(self)
    % Node size
    params.nG = 4;
    params.nS = 4;
    params.nF = 2;
    params.nX = 2;
    
    % Node label
    params.G1 = 1; 
    params.S1 = 2; 
    params.F1 = 3; 
    params.X1 = 4; 

    params.onodes = [params.G1 params.F1 params.X1];
      
    params.Gstartprob = ones(1, params.nG) / params.nG;
    params.Gtransprob = [0 0.45 0.45 0.1
                         0 0.1 0.1 0.8
                         0 0.1 0.1 0.8
                         1 0 0 0];
    
    % P_ij = P(S = j | G = i)
    params.Sstartprob = [1 0 0 0
                         0 1 0 0
                         0 0 1 0
                         0 0 0 1];
                       
    params.Stransprob = zeros(params.nS, params.nG, params.nS);
    params.Stransprob(:, 1, :) = [1 0 0 0 
                                  1 0 0 0 
                                  1 0 0 0 
                                  1 0 0 0];
    params.Stransprob(:, 2, :) = [0 1 0 0 
                                  0 1 0 0
                                  0 1 0 0
                                  0 1 0 0];

    params.Stransprob(:, 3, :) = [0 0 1 0 
                                  0 0 1 0
                                  0 0 1 0
                                  0 0 1 0];
                                
    params.Stransprob(:, 4, :) = [0 0 0 1 
                                  0 0 0 1
                                  0 0 0 1
                                  0 0 0 1];

    params.Stermprob = ones(params.nG, params.nS, params.nF) / params.nF;
                             
    params.hand = ones(self.hand_size, params.nS);
 
    params.hd_mu = 0;
    params.hd_sigma = 1;

    params.Xmean = ones(params.nX, params.nS);
    params.Xcov = repmat(eye(params.nX) * 0.1, [1, 1, params.nS]);
  end
end

methods(Static)
  function map = getMarginals(engine, hnodes, T)
    map = zeros(length(hnodes), T);
    for t = 1 : T
      for n = hnodes(:)'
        m = marginal_nodes(engine, n, t);
        [~, index] = max(m.T);
        map(n, t) = index;
      end
    end
  end
end
 
end
