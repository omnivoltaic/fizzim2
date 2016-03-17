mkdir -p ./test
cp -r ./example/*.fzm ./test

jar=`ls Fizzim2-*.jar`

cd ./test
for f in `ls *.fzm`
do
    java -jar ../$jar -batch_rewrite $f
done

n=0
t=0

for f in `ls *.v`
do
    sed -i '1d' $f
    diff ../example/$f $f

    if test $? -eq 0
    then
        printf "  %-30s OK\n" $f
        let n++
    else
        printf "\nError in '%s'\n" $f
    fi
    let t++
done

echo -e '\n'$n/$t Passed.
