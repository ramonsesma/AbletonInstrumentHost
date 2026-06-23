AIHResult {
    *mergeData { |result, data|
        var reservedKeys;

        reservedKeys = [\ok, \family, \message];
        (data ? ()).keysValuesDo { |key, value|
            if(reservedKeys.includes(key).not) {
                result[key] = value;
            };
        };
        ^result
    }

    *success { |data|
        var result;

        result = IdentityDictionary.new;
        result[\ok] = true;
        ^this.mergeData(result, data)
    }

    *failure { |family, message, data|
        var result;

        result = IdentityDictionary.new;
        result[\ok] = false;
        result[\family] = family;
        result[\message] = message.asString;
        ^this.mergeData(result, data)
    }

    *report { |item|
        ^item.asCompileString
    }
}

