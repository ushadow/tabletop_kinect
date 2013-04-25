function loss = zerooneloss(y, ystar)
if y == ystar
  loss = 0;
else
  loss = 1;
end