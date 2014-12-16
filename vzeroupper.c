#include <immintrin.h>
#include <stdio.h>

int main()
{
  int status = _XBEGIN_STARTED;
  int retries = 10000000;
  __builtin_ia32_vzeroupper();
 restart:
  status = _xbegin();
  if (status != _XBEGIN_STARTED) {
    if (retries-- > 0)
      goto restart;
    printf("Failed\n");
    return 1;
  }
  __builtin_ia32_vzeroupper();
  _xend();

  printf("Success\n");
  return 0;
}
