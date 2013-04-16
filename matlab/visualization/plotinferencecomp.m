function plotinferencecomp
L = [0 5 10 20 53];
accuracy = [66.4 82.4 83.4 83.8 85.1];
E = [17 15 14 15 15];
plot(L, accuracy);
ylabel('average frame classification accuracy %', 'FontSize', 12);
xlabel('lag / frames', 'FontSize', 12);
end