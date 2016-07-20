mkdir -p ./test
cp -r ./example/*.fzm ./test

jar=`ls Fizzim2-*.jar`

cd ./test
for f in `ls *.fzm`
do
    java -jar ../$jar -batch_rewrite $f

    # change 'file.fzm' to 'file.v'
    # delete 1st line
    sed -i '1d' ./${f%.*}.v
done

n=0
t=0

for f in `ls *.v`
do
    cp ../example/bench/$f tv
    diff tv $f

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

if test $n -eq $t
then
    cd ..
    rm -rf ./test
fi
