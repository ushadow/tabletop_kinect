library(mclust)

# Args:
# input.file: string of input file name. The data should be comma delimited and
# the first column is the frame ID.
Cluster <- function(input.file, output.file) {
  data <- read.table(input.file, sep = ',')
  numcol <- ncol(data)
  descriptor.mclust <- Mclust(data[, 2 : numcol], G = 1:2)
  output <- data.frame(data[, 1], descriptor.mclust$classification)
  write.table(output, output.file, sep = ',', row.names = FALSE, col.names =
              FALSE)
}

