=>(start)-openfile->(running)
(running)-change->(running)
(running)-save->(quit)
(start)-eps->(start)
(running)-eps->(running)
(quit)-eps->(quit)
(start)-main->[nope]
(start)-change->[nope]
(start)-save->[nope]
(running)-main->[nope]
(running)-openfile->[nope]
(quit)-main->[nope]
(quit)-openfile->[nope]
(quit)-change->[nope]
(quit)-save->[nope]
[nope]-eps->[nope]